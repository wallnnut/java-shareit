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
class BookingShortDtoJsonTest {
    private final JacksonTester<BookingShortDto> json;

    @Test
    void testSerialize() throws Exception {
        BookingShortDto dto = new BookingShortDto(
                2L,
                3L,
                LocalDateTime.of(2026, 10, 12, 10, 15, 0),
                LocalDateTime.of(2026, 10, 13, 11, 0, 0));

        JsonContent<BookingShortDto> result = json.write(dto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(dto.getId().intValue());
        assertThat(result).extractingJsonPathNumberValue("$.bookerId").isEqualTo(dto.getBookerId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2026-10-12T10:15:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2026-10-13T11:00:00");
    }
}
