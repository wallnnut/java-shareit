package ru.practicum.shareit.request.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

@JsonTest
class ItemRequestDtoJsonTest {
    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void createdUsesDateTimeFormat() throws Exception {
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("need a drill")
                .created(LocalDateTime.of(2026, 10, 12, 15, 30, 0))
                .items(List.of(ItemResponseDto.builder().id(2L).name("Drill").ownerId(3L).build()))
                .build();

        assertThat(json.write(dto)).extractingJsonPathStringValue("$.created")
                .isEqualTo("2026-10-12T15:30:00");
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.description")
                .isEqualTo("need a drill");
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.items[0].name")
                .isEqualTo("Drill");
    }
}
