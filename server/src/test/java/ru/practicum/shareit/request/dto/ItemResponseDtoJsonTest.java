package ru.practicum.shareit.request.dto;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemResponseDtoJsonTest {
    private final JacksonTester<ItemResponseDto> json;

    @Test
    void testSerialize() throws Exception {
        ItemResponseDto dto = new ItemResponseDto(2L, "Drill", 3L);

        JsonContent<ItemResponseDto> result = json.write(dto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(dto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(dto.getName());
        assertThat(result).extractingJsonPathNumberValue("$.ownerId").isEqualTo(dto.getOwnerId().intValue());
    }
}
