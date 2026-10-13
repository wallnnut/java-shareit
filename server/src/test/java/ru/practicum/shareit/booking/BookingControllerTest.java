package ru.practicum.shareit.booking;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    private static final String USER_HEADER = "X-Sharer-User-Id";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Test
    void bookingRoutes() throws Exception {
        BookingDto dto = BookingDto.builder().id(8L).status(BookingStatus.WAITING).build();
        when(bookingService.create(eq(1L), any())).thenReturn(dto);
        when(bookingService.approve(1L, 8L, true)).thenReturn(dto);
        when(bookingService.getById(1L, 8L)).thenReturn(dto);
        when(bookingService.getByBooker(1L, "ALL")).thenReturn(List.of(dto));
        when(bookingService.getByOwner(1L, "ALL")).thenReturn(List.of(dto));

        String start = LocalDateTime.now().plusDays(1).format(FORMATTER);
        String end = LocalDateTime.now().plusDays(2).format(FORMATTER);
        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":4,\"start\":\"" + start + "\",\"end\":\"" + end + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8));
        mockMvc.perform(patch("/bookings/8").header(USER_HEADER, 1).param("approved", "true"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/bookings/8").header(USER_HEADER, 1)).andExpect(status().isOk());
        mockMvc.perform(get("/bookings").header(USER_HEADER, 1)).andExpect(status().isOk());
        mockMvc.perform(get("/bookings/owner").header(USER_HEADER, 1)).andExpect(status().isOk());

        verify(bookingService).create(eq(1L), any(BookItemRequestDto.class));
        verify(bookingService).approve(1L, 8L, true);
        verify(bookingService).getById(1L, 8L);
        verify(bookingService).getByBooker(1L, "ALL");
        verify(bookingService).getByOwner(1L, "ALL");
    }
}
