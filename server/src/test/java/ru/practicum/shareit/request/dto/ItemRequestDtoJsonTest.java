package ru.practicum.shareit.request.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.validation.Create;

@JsonTest
class ItemRequestDtoJsonTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void createdUsesDateTimeFormat() throws Exception {
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("need a drill")
                .created(LocalDateTime.of(2026, 10, 12, 15, 30, 0))
                .items(List.of(ItemRequestAnswerDto.builder().id(2L).name("Drill").ownerId(3L).build()))
                .build();

        assertThat(json.write(dto)).extractingJsonPathStringValue("$.created")
                .isEqualTo("2026-10-12T15:30:00");
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.description")
                .isEqualTo("need a drill");
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.items[0].name")
                .isEqualTo("Drill");
    }

    @Test
    void blankDescriptionIsInvalid() {
        ItemRequestDto blank = ItemRequestDto.builder().description("  ").build();
        ItemRequestDto filled = ItemRequestDto.builder().description("need a drill").build();

        assertThat(validator.validate(blank, Create.class)).isNotEmpty();
        assertThat(validator.validate(filled, Create.class)).isEmpty();
    }
}
