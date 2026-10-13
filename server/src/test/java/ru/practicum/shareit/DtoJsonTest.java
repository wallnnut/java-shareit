package ru.practicum.shareit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

@JsonTest
class DtoJsonTest {
    @Autowired
    private JacksonTester<BookItemRequestDto> bookItemRequestJson;
    @Autowired
    private JacksonTester<BookingDto> bookingJson;
    @Autowired
    private JacksonTester<BookingShortDto> bookingShortJson;
    @Autowired
    private JacksonTester<CommentDto> commentJson;
    @Autowired
    private JacksonTester<UserDto> userJson;
    @Autowired
    private JacksonTester<ItemDto> itemJson;

    @Test
    void dateFieldsUseLocalDateTimeFormat() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 10, 12, 10, 15, 0);
        LocalDateTime end = LocalDateTime.of(2026, 10, 13, 11, 0, 0);

        BookItemRequestDto request = BookItemRequestDto.builder().itemId(4L).start(start).end(end).build();
        assertThat(bookItemRequestJson.write(request)).extractingJsonPathStringValue("$.start").isEqualTo("2026-10-12T10:15:00");
        assertThat(bookItemRequestJson.write(request)).extractingJsonPathStringValue("$.end").isEqualTo("2026-10-13T11:00:00");

        BookingDto booking = BookingDto.builder().id(1L).start(start).end(end).status(BookingStatus.WAITING).build();
        assertThat(bookingJson.write(booking)).extractingJsonPathStringValue("$.start").isEqualTo("2026-10-12T10:15:00");
        assertThat(bookingJson.write(booking)).extractingJsonPathStringValue("$.end").isEqualTo("2026-10-13T11:00:00");

        BookingShortDto shortDto = BookingShortDto.builder().id(2L).bookerId(3L).start(start).end(end).build();
        assertThat(bookingShortJson.write(shortDto)).extractingJsonPathStringValue("$.start")
                .isEqualTo("2026-10-12T10:15:00");
        assertThat(bookingShortJson.write(shortDto)).extractingJsonPathNumberValue("$.bookerId").isEqualTo(3);

        CommentDto comment = CommentDto.builder().id(5L).text("Nice").authorName("Ann").created(start).build();
        assertThat(commentJson.write(comment)).extractingJsonPathStringValue("$.created").isEqualTo("2026-10-12T10:15:00");
    }

    @Test
    void serializesUserEmailAndItemRequestId() throws Exception {
        UserDto validUser = UserDto.builder().name("Ann").email("ann@mail.com").build();
        assertThat(userJson.write(validUser)).extractingJsonPathStringValue("$.email").isEqualTo("ann@mail.com");

        ItemDto validItem = ItemDto.builder().name("Drill").description("Power").available(true).requestId(9L).build();
        assertThat(itemJson.write(validItem)).extractingJsonPathNumberValue("$.requestId").isEqualTo(9);
    }
}
