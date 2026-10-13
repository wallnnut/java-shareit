package ru.practicum.shareit.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

@SpringBootTest
@Transactional
class ItemServiceIntegrationTest {
    @Autowired
    private ItemService itemService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserService userService;

    @Test
    void updateGetSearchAndComments() {
        long ownerId = user("Owner", "item-owner@mail.com");
        long otherId = user("Other", "item-other@mail.com");
        long bookerId = user("Booker", "item-booker@mail.com");
        long emptyOwnerId = user("Empty", "item-empty@mail.com");

        ItemDto created = itemService.create(ownerId, item("Drill", "Power drill", true));
        itemService.create(ownerId, item("Saw", "Hidden saw", false));
        long itemId = created.getId();

        ItemDto renamed = itemService.update(ownerId, itemId, ItemDto.builder().name("Drill Pro").build());
        assertThat(renamed.getName()).isEqualTo("Drill Pro");
        assertThat(renamed.getDescription()).isEqualTo("Power drill");

        ItemDto described = itemService.update(ownerId, itemId, ItemDto.builder().description("Better").build());
        assertThat(described.getDescription()).isEqualTo("Better");

        ItemDto hidden = itemService.update(ownerId, itemId,
                ItemDto.builder().name("  ").description("").available(false).build());
        assertThat(hidden.getName()).isEqualTo("Drill Pro");
        assertThat(hidden.getAvailable()).isFalse();
        itemService.update(ownerId, itemId, ItemDto.builder().available(true).build());

        assertThatThrownBy(() -> itemService.update(otherId, itemId, ItemDto.builder().name("Nope").build()))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> itemService.update(ownerId, 9999L, ItemDto.builder().name("Nope").build()))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> itemService.getById(9999L, itemId)).isInstanceOf(NotFoundException.class);

        bookingService.create(bookerId, booking(itemId, LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(1)));
        var waiting = bookingService.create(bookerId, booking(itemId, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)));
        bookingService.approve(ownerId, waiting.getId(), true);

        ItemDto forOwner = itemService.getById(ownerId, itemId);
        ItemDto forOther = itemService.getById(otherId, itemId);
        assertThat(forOwner.getNextBooking()).isNotNull();
        assertThat(forOther.getNextBooking()).isNull();
        assertThat(forOther.getName()).isEqualTo("Drill Pro");

        assertThat(itemService.getAllByOwner(emptyOwnerId)).isEmpty();
        assertThat(itemService.getAllByOwner(ownerId)).extracting(ItemDto::getName)
                .contains("Drill Pro", "Saw");
        assertThat(itemService.getAllByOwner(ownerId).get(0).getNextBooking()).isNotNull();

        assertThat(itemService.search(otherId, "drill")).extracting(ItemDto::getId).contains(itemId);
        assertThat(itemService.search(otherId, "saw")).isEmpty();
        assertThat(itemService.search(otherId, " ")).isEmpty();
        assertThat(itemService.search(otherId, null)).isEmpty();
        assertThatThrownBy(() -> itemService.search(9999L, "drill")).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> itemService.getAllByOwner(9999L)).isInstanceOf(NotFoundException.class);

        CommentDto early = CommentDto.builder().text("Too early").build();
        assertThatThrownBy(() -> itemService.addComment(bookerId, itemId, early))
                .isInstanceOf(IllegalArgumentException.class);

        var past = bookingService.create(bookerId, booking(itemId, LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(2)));
        bookingService.approve(ownerId, past.getId(), true);
        CommentDto saved = itemService.addComment(bookerId, itemId, CommentDto.builder().text("Great").build());
        assertThat(saved.getText()).isEqualTo("Great");
        assertThat(saved.getAuthorName()).isEqualTo("Booker");
        assertThat(itemService.getById(ownerId, itemId).getLastBooking().getId()).isEqualTo(past.getId());
        assertThat(itemService.getById(ownerId, itemId).getComments()).hasSize(1);
    }

    private long user(String name, String email) {
        return userService.create(UserDto.builder().name(name).email(email).build()).getId();
    }

    private ItemDto item(String name, String description, boolean available) {
        return ItemDto.builder().name(name).description(description).available(available).build();
    }

    private BookItemRequestDto booking(long itemId, LocalDateTime start, LocalDateTime end) {
        return BookItemRequestDto.builder().itemId(itemId).start(start).end(end).build();
    }
}
