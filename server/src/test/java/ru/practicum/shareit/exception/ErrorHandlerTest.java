package ru.practicum.shareit.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = ErrorHandlerTest.Api.class)
@Import({ErrorHandler.class, ErrorHandlerTest.Api.class})
class ErrorHandlerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void mapsErrorsToStatusAndErrorField() throws Exception {
        mockMvc.perform(get("/err/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("missing"));
        mockMvc.perform(get("/err/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("dup"));
        mockMvc.perform(get("/err/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("no"));
        mockMvc.perform(get("/err/unavailable"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("busy"));
        mockMvc.perform(get("/err/illegal"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("bad"));
        mockMvc.perform(get("/err/type/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
        mockMvc.perform(post("/err/valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
        mockMvc.perform(post("/err/header")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ann\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
        mockMvc.perform(get("/err/param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
        mockMvc.perform(post("/err/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
        mockMvc.perform(get("/err/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("boom"));
    }

    @RestController
    public static class Api {
        @GetMapping("/err/not-found")
        public void notFound() {
            throw new NotFoundException("missing");
        }

        @GetMapping("/err/conflict")
        public void conflict() {
            throw new ConflictException("dup");
        }

        @GetMapping("/err/forbidden")
        public void forbidden() {
            throw new ForbiddenException("no");
        }

        @GetMapping("/err/unavailable")
        public void unavailable() {
            throw new BookingUnavailableException("busy");
        }

        @GetMapping("/err/illegal")
        public void illegal() {
            throw new IllegalArgumentException("bad");
        }

        @GetMapping("/err/type/{id}")
        public void type(@PathVariable Long id) {
            if (id == null) {
                throw new IllegalArgumentException("id");
            }
        }

        @PostMapping("/err/valid")
        public void valid(@Valid @RequestBody NameBody body) {
            if (body == null) {
                throw new IllegalArgumentException("body");
            }
        }

        @PostMapping("/err/header")
        public void header(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody NameBody body) {
            if (userId == null || body == null) {
                throw new IllegalArgumentException("header");
            }
        }

        @GetMapping("/err/param")
        public void param(@RequestParam String text) {
            if (text == null) {
                throw new IllegalArgumentException("text");
            }
        }

        @PostMapping("/err/body")
        public void body(@RequestBody NameBody body) {
            if (body == null) {
                throw new IllegalArgumentException("body");
            }
        }

        @GetMapping("/err/boom")
        public void boom() {
            throw new RuntimeException("boom");
        }
    }

    @Getter
    @Setter
    static class NameBody {
        @NotBlank
        private String name;
    }
}
