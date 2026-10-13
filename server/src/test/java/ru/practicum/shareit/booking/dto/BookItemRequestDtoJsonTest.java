package ru.practicum.shareit.booking.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookItemRequestDtoJsonTest {
    private final JacksonTester<BookItemRequestDto> json;

    @Test
    void testSerialize() throws Exception {
        BookItemRequestDto dto = new BookItemRequestDto(
                4L,
                LocalDateTime.of(2026, 10, 12, 10, 15, 0),
                LocalDateTime.of(2026, 10, 13, 11, 0, 0));

        JsonContent<BookItemRequestDto> result = json.write(dto);
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(dto.getItemId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2026-10-12T10:15:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2026-10-13T11:00:00");
    }
}
