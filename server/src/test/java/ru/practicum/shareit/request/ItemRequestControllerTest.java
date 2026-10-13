package ru.practicum.shareit.request;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestAnswerDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    void createReturnsRequest() throws Exception {
        when(itemRequestService.create(eq(1L), any())).thenReturn(ItemRequestDto.builder()
                .id(5L)
                .description("need drill")
                .created(LocalDateTime.of(2026, 1, 2, 3, 4, 5))
                .items(List.of())
                .build());

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"need drill\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.description").value("need drill"))
                .andExpect(jsonPath("$.created").value("2026-01-02T03:04:05"))
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void getOwnReturnsRequests() throws Exception {
        when(itemRequestService.getOwn(1L)).thenReturn(List.of(
                ItemRequestDto.builder().id(2L).description("b").items(List.of()).build(),
                ItemRequestDto.builder().id(1L).description("a").items(List.of()).build()));

        mockMvc.perform(get("/requests").header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        verify(itemRequestService).getOwn(1L);
    }

    @Test
    void getAllIsNotTreatedAsRequestId() throws Exception {
        when(itemRequestService.getAll(1L)).thenReturn(List.of());

        mockMvc.perform(get("/requests/all").header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        verify(itemRequestService).getAll(1L);
    }

    @Test
    void getByIdReturnsItems() throws Exception {
        when(itemRequestService.getById(2L, 5L)).thenReturn(ItemRequestDto.builder()
                .id(5L)
                .description("need drill")
                .created(LocalDateTime.of(2026, 1, 2, 3, 4, 5))
                .items(List.of(ItemRequestAnswerDto.builder().id(7L).name("Drill").ownerId(3L).build()))
                .build());

        mockMvc.perform(get("/requests/5").header(USER_HEADER, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("Drill"))
                .andExpect(jsonPath("$.items[0].ownerId").value(3));
    }
}
