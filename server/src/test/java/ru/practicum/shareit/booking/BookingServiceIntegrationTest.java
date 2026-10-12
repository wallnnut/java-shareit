package ru.practicum.shareit.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.BookingUnavailableException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

@SpringBootTest
@Transactional
class BookingServiceIntegrationTest {
    @Autowired
    private BookingService bookingService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;

    @Test
    void createApproveAndReadBookings() {
        long ownerId = user("Owner", "book-owner@mail.com");
        long bookerId = user("Booker", "book-booker@mail.com");
        long strangerId = user("Stranger", "book-stranger@mail.com");
        long itemId = item(ownerId, "Drill", true);
        long hiddenId = item(ownerId, "Saw", false);

        assertThatThrownBy(() -> bookingService.create(bookerId,
                booking(itemId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> bookingService.create(9999L,
                booking(itemId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> bookingService.create(bookerId,
                booking(9999L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> bookingService.create(bookerId,
                booking(hiddenId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))))
                .isInstanceOf(BookingUnavailableException.class);
        assertThatThrownBy(() -> bookingService.create(ownerId,
                booking(itemId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))))
                .isInstanceOf(NotFoundException.class);

        BookingDto future = bookingService.create(bookerId,
                booking(itemId, LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3)));
        assertThat(future.getStatus()).isEqualTo(BookingStatus.WAITING);
        BookingDto rejected = bookingService.create(bookerId,
                booking(itemId, LocalDateTime.now().plusDays(4), LocalDateTime.now().plusDays(5)));
        BookingDto current = bookingService.create(bookerId,
                booking(itemId, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1)));
        BookingDto past = bookingService.create(bookerId,
                booking(itemId, LocalDateTime.now().minusDays(4), LocalDateTime.now().minusDays(3)));
        BookingDto overlap = bookingService.create(bookerId,
                booking(itemId, LocalDateTime.now().plusDays(2).plusHours(1), LocalDateTime.now().plusDays(2).plusHours(5)));

        assertThatThrownBy(() -> bookingService.approve(bookerId, future.getId(), true))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> bookingService.approve(9999L, future.getId(), true))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> bookingService.approve(ownerId, 9999L, true))
                .isInstanceOf(NotFoundException.class);

        assertThat(bookingService.approve(ownerId, rejected.getId(), false).getStatus())
                .isEqualTo(BookingStatus.REJECTED);
        assertThatThrownBy(() -> bookingService.approve(ownerId, rejected.getId(), true))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(bookingService.approve(ownerId, future.getId(), true).getStatus())
                .isEqualTo(BookingStatus.APPROVED);
        assertThatThrownBy(() -> bookingService.approve(ownerId, overlap.getId(), true))
                .isInstanceOf(IllegalArgumentException.class);
        bookingService.approve(ownerId, current.getId(), true);
        bookingService.approve(ownerId, past.getId(), true);

        assertThat(bookingService.getById(bookerId, future.getId()).getId()).isEqualTo(future.getId());
        assertThat(bookingService.getById(ownerId, future.getId()).getItem().getId()).isEqualTo(itemId);
        assertThatThrownBy(() -> bookingService.getById(strangerId, future.getId()))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> bookingService.getById(bookerId, 9999L)).isInstanceOf(NotFoundException.class);

        assertThat(bookingService.getByBooker(bookerId, "ALL")).extracting(BookingDto::getId)
                .contains(future.getId(), rejected.getId(), current.getId(), past.getId(), overlap.getId());
        assertThat(bookingService.getByBooker(bookerId, "FUTURE")).extracting(BookingDto::getId)
                .contains(future.getId(), rejected.getId(), overlap.getId());
        assertThat(bookingService.getByBooker(bookerId, "WAITING")).extracting(BookingDto::getId)
                .contains(overlap.getId())
                .doesNotContain(future.getId());
        assertThat(bookingService.getByBooker(bookerId, "CURRENT")).extracting(BookingDto::getId)
                .contains(current.getId());
        assertThat(bookingService.getByBooker(bookerId, "PAST")).extracting(BookingDto::getId)
                .contains(past.getId());
        assertThat(bookingService.getByBooker(bookerId, "REJECTED")).extracting(BookingDto::getId)
                .contains(rejected.getId());

        assertThat(bookingService.getByOwner(ownerId, "ALL")).isNotEmpty();
        assertThat(bookingService.getByOwner(ownerId, "FUTURE")).extracting(BookingDto::getId).contains(future.getId());
        assertThat(bookingService.getByOwner(ownerId, "CURRENT")).extracting(BookingDto::getId).contains(current.getId());
        assertThat(bookingService.getByOwner(ownerId, "PAST")).extracting(BookingDto::getId).contains(past.getId());
        assertThat(bookingService.getByOwner(ownerId, "WAITING")).extracting(BookingDto::getId).contains(overlap.getId());
        assertThat(bookingService.getByOwner(ownerId, "REJECTED")).extracting(BookingDto::getId)
                .contains(rejected.getId());

        assertThatThrownBy(() -> bookingService.getByBooker(9999L, "ALL")).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> bookingService.getByOwner(9999L, "ALL")).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> bookingService.getByBooker(bookerId, "UNKNOWN"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> bookingService.getByOwner(ownerId, " ")).isInstanceOf(IllegalArgumentException.class);
    }

    private long user(String name, String email) {
        return userService.create(UserDto.builder().name(name).email(email).build()).getId();
    }

    private long item(long ownerId, String name, boolean available) {
        return itemService.create(ownerId, ItemDto.builder()
                .name(name)
                .description(name + " description")
                .available(available)
                .build()).getId();
    }

    private BookItemRequestDto booking(long itemId, LocalDateTime start, LocalDateTime end) {
        return BookItemRequestDto.builder().itemId(itemId).start(start).end(end).build();
    }
}
