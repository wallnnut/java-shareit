package ru.practicum.shareit.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import ru.practicum.shareit.user.dto.UserDto;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void userRoutes() throws Exception {
        when(userService.create(any())).thenReturn(UserDto.builder().id(1L).name("Ann").email("ann@mail.com").build());
        when(userService.update(eq(1L), any())).thenReturn(UserDto.builder().id(1L).name("Anna").email("ann@mail.com").build());
        when(userService.getById(1L)).thenReturn(UserDto.builder().id(1L).name("Ann").email("ann@mail.com").build());
        when(userService.getAll()).thenReturn(List.of());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ann\",\"email\":\"ann@mail.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Anna\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Anna"));
        mockMvc.perform(get("/users/1")).andExpect(status().isOk());
        mockMvc.perform(get("/users")).andExpect(status().isOk()).andExpect(jsonPath("$").isArray());
        mockMvc.perform(delete("/users/1")).andExpect(status().isOk());

        verify(userService).create(any(UserDto.class));
        verify(userService).update(eq(1L), any(UserDto.class));
        verify(userService).getById(1L);
        verify(userService).getAll();
        verify(userService).deleteById(1L);
    }

    @Test
    void invalidUserIsBadRequest() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"bad\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
        verifyNoInteractions(userService);
    }
}
