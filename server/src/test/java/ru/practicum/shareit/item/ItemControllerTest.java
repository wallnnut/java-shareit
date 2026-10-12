package ru.practicum.shareit.item;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

@WebMvcTest(ItemController.class)
class ItemControllerTest {
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Test
    void itemRoutes() throws Exception {
        when(itemService.update(eq(1L), eq(3L), any())).thenReturn(ItemDto.builder().id(3L).name("Drill").build());
        when(itemService.getById(1L, 3L)).thenReturn(ItemDto.builder().id(3L).name("Drill").build());
        when(itemService.getAllByOwner(1L)).thenReturn(List.of(ItemDto.builder().id(3L).name("Drill").build()));
        when(itemService.search(1L, "drill")).thenReturn(List.of());
        when(itemService.addComment(eq(1L), eq(3L), any()))
                .thenReturn(CommentDto.builder().id(4L).text("Nice").authorName("Ann").build());

        mockMvc.perform(patch("/items/3")
                        .header(USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Drill\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Drill"));
        mockMvc.perform(get("/items/3").header(USER_HEADER, 1))
                .andExpect(status().isOk());
        mockMvc.perform(get("/items").header(USER_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3));
        mockMvc.perform(get("/items/search").header(USER_HEADER, 1).param("text", "drill"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/items/3/comment")
                        .header(USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Nice\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorName").value("Ann"));

        verify(itemService).update(eq(1L), eq(3L), any(ItemDto.class));
        verify(itemService).getById(1L, 3L);
        verify(itemService).getAllByOwner(1L);
        verify(itemService).search(1L, "drill");
        verify(itemService).addComment(eq(1L), eq(3L), any(CommentDto.class));
    }
}
