package ru.practicum.shareit.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;

@WebMvcTest(ItemController.class)
class ItemControllerCreateTest {
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Test
    void createWithoutRequestId() throws Exception {
        when(itemService.create(eq(1L), any())).thenReturn(ItemDto.builder()
                .id(3L)
                .name("Drill")
                .description("Power")
                .available(true)
                .build());

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Drill\",\"description\":\"Power\",\"available\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Drill"));

        ArgumentCaptor<ItemDto> captor = ArgumentCaptor.forClass(ItemDto.class);
        verify(itemService).create(eq(1L), captor.capture());
        assertThat(captor.getValue().getRequestId()).isNull();
    }

    @Test
    void createWithRequestId() throws Exception {
        when(itemService.create(eq(1L), any())).thenReturn(ItemDto.builder()
                .id(4L)
                .name("Drill")
                .description("Power")
                .available(true)
                .requestId(9L)
                .build());

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Drill\",\"description\":\"Power\",\"available\":true,\"requestId\":9}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(9));

        ArgumentCaptor<ItemDto> captor = ArgumentCaptor.forClass(ItemDto.class);
        verify(itemService).create(eq(1L), captor.capture());
        assertThat(captor.getValue().getRequestId()).isEqualTo(9L);
    }
}
