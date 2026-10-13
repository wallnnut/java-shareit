package ru.practicum.shareit.booking.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.BookingStatus;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingDtoJsonTest {
    private final JacksonTester<BookingDto> json;

    @Test
    void testSerialize() throws Exception {
        BookingDto dto = BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.of(2026, 10, 12, 10, 15, 0))
                .end(LocalDateTime.of(2026, 10, 13, 11, 0, 0))
                .status(BookingStatus.WAITING)
                .build();

        JsonContent<BookingDto> result = json.write(dto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(dto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2026-10-12T10:15:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2026-10-13T11:00:00");
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo(dto.getStatus().name());
    }
}
