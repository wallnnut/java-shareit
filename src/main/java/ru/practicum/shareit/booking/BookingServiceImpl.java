package ru.practicum.shareit.booking;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.BookingUnavailableException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private static final Sort START_DESC = Sort.by(Sort.Direction.DESC, "start");

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingDto create(Long userId, BookItemRequestDto request) {
        validateEndAfterStart(request.getStart(), request.getEnd());
        User booker = getUserOrThrow(userId);
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена: " + request.getItemId()));
        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new BookingUnavailableException("Вещь недоступна для бронирования");
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец не может забронировать свою вещь");
        }
        assertNoApprovedOverlap(item.getId(), request.getStart(), request.getEnd());

        Booking booking = Booking.builder()
                .start(request.getStart())
                .end(request.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto approve(Long userId, Long bookingId, boolean approved) {
        boolean userExists = userRepository.existsById(userId);
        Booking booking = getBookingOrThrow(bookingId);
        if (!userExists || !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Подтвердить бронирование может только владелец вещи");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new IllegalArgumentException("Бронирование уже рассмотрено");
        }
        if (approved) {
            assertNoApprovedOverlap(booking.getItem().getId(), booking.getStart(), booking.getEnd());
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);
        if (!isBooker && !isOwner) {
            throw new NotFoundException("Бронирование не найдено: " + bookingId);
        }
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getByBooker(Long userId, String state) {
        getUserOrThrow(userId);
        return findByBooker(userId, BookingState.from(state)).stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    @Override
    public List<BookingDto> getByOwner(Long userId, String state) {
        getUserOrThrow(userId);
        return findByOwner(userId, BookingState.from(state)).stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    private List<Booking> findByBooker(Long userId, BookingState state) {
        LocalDateTime now = LocalDateTime.now();
        return switch (state) {
            case ALL -> bookingRepository.findByBookerId(userId, START_DESC);
            case CURRENT -> bookingRepository
                    .findByBookerIdAndStartLessThanEqualAndEndGreaterThanEqual(userId, now, now, START_DESC);
            case PAST -> bookingRepository.findByBookerIdAndEndIsBefore(userId, now, START_DESC);
            case FUTURE -> bookingRepository.findByBookerIdAndStartIsAfter(userId, now, START_DESC);
            case WAITING -> bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING, START_DESC);
            case REJECTED -> bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED, START_DESC);
        };
    }

    private List<Booking> findByOwner(Long userId, BookingState state) {
        LocalDateTime now = LocalDateTime.now();
        return switch (state) {
            case ALL -> bookingRepository.findByItem_OwnerId(userId, START_DESC);
            case CURRENT -> bookingRepository
                    .findByItem_OwnerIdAndStartLessThanEqualAndEndGreaterThanEqual(userId, now, now, START_DESC);
            case PAST -> bookingRepository.findByItem_OwnerIdAndEndIsBefore(userId, now, START_DESC);
            case FUTURE -> bookingRepository.findByItem_OwnerIdAndStartIsAfter(userId, now, START_DESC);
            case WAITING -> bookingRepository.findByItem_OwnerIdAndStatus(userId, BookingStatus.WAITING, START_DESC);
            case REJECTED -> bookingRepository
                    .findByItem_OwnerIdAndStatus(userId, BookingStatus.REJECTED, START_DESC);
        };
    }

    private void validateEndAfterStart(LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Дата окончания должна быть позже даты начала");
        }
    }

    private void assertNoApprovedOverlap(Long itemId, LocalDateTime start, LocalDateTime end) {
        boolean overlaps = bookingRepository.findByItemIdAndStatus(itemId, BookingStatus.APPROVED).stream()
                .anyMatch(booking -> booking.getStart().isBefore(end) && booking.getEnd().isAfter(start));
        if (overlaps) {
            throw new IllegalArgumentException("Вещь уже забронирована на выбранные даты");
        }
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено: " + bookingId));
    }
}
