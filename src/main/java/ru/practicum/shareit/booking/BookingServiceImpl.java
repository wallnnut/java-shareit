package ru.practicum.shareit.booking;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
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
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingDto create(Long userId, BookItemRequestDto request) {
        validateDates(request.getStart(), request.getEnd());
        User booker = getUserOrThrow(userId);
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена: " + request.getItemId()));
        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new IllegalArgumentException("Вещь недоступна для бронирования");
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
        Booking booking = getBookingOrThrow(bookingId);
        if (!booking.getItem().getOwner().getId().equals(userId)) {
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
        BookingState bookingState = BookingState.from(state);
        return filterByState(bookingRepository.findByBookerIdOrderByStartDesc(userId), bookingState).stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    @Override
    public List<BookingDto> getByOwner(Long userId, String state) {
        getUserOrThrow(userId);
        BookingState bookingState = BookingState.from(state);
        return filterByState(bookingRepository.findByItemOwnerIdOrderByStartDesc(userId), bookingState).stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    private void validateDates(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        if (start.isBefore(now) || end.isBefore(now)) {
            throw new IllegalArgumentException("Даты бронирования не могут быть в прошлом");
        }
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

    private List<Booking> filterByState(List<Booking> bookings, BookingState state) {
        LocalDateTime now = LocalDateTime.now();
        return switch (state) {
            case ALL -> bookings;
            case CURRENT -> bookings.stream()
                    .filter(booking -> !booking.getStart().isAfter(now) && !booking.getEnd().isBefore(now))
                    .toList();
            case PAST -> bookings.stream()
                    .filter(booking -> booking.getEnd().isBefore(now))
                    .toList();
            case FUTURE -> bookings.stream()
                    .filter(booking -> booking.getStart().isAfter(now))
                    .toList();
            case WAITING -> bookings.stream()
                    .filter(booking -> booking.getStatus() == BookingStatus.WAITING)
                    .toList();
            case REJECTED -> bookings.stream()
                    .filter(booking -> booking.getStatus() == BookingStatus.REJECTED)
                    .toList();
        };
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
