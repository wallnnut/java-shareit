package ru.practicum.shareit.request.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestDtoJsonTest {
    private final JacksonTester<ItemRequestDto> json;

    @Test
    void testSerialize() throws Exception {
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("need a drill")
                .created(LocalDateTime.of(2026, 10, 12, 15, 30, 0))
                .items(List.of(ItemResponseDto.builder().id(2L).name("Drill").ownerId(3L).build()))
                .build();

        JsonContent<ItemRequestDto> result = json.write(dto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(dto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(dto.getDescription());
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2026-10-12T15:30:00");
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Drill");
        assertThat(result).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(3);
    }
}
