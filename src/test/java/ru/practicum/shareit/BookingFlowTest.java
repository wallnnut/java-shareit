package ru.practicum.shareit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class BookingFlowTest {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void bookingsAndCommentsFlow() throws Exception {
        long ownerId = createUser("Owner", "owner@mail.com");
        long bookerId = createUser("Booker", "booker@mail.com");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Other\",\"email\":\"owner@mail.com\"}"))
                .andExpect(status().isConflict());

        long itemId = createItem(ownerId, "Drill", "Power drill", true);
        long hiddenId = createItem(ownerId, "Saw", "Hidden saw", false);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"description\":\"Y\",\"available\":true}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 9999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"description\":\"Y\",\"available\":true}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/items/search").header(USER_HEADER, bookerId).param("text", "DRILL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Drill"));
        mockMvc.perform(get("/items/search").header(USER_HEADER, bookerId).param("text", "saw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/items/search").header(USER_HEADER, bookerId).param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        String start = LocalDateTime.now().plusDays(1).format(FORMATTER);
        String end = LocalDateTime.now().plusDays(2).format(FORMATTER);
        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(hiddenId, start, end)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 9999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(itemId, start, end)))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(9999, start, end)))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(itemId, start, start)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":" + itemId + ",\"end\":\"" + end + "\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(itemId, start, end)))
                .andExpect(status().isNotFound());

        MvcResult created = mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(itemId, start, end)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.start").value(start))
                .andExpect(jsonPath("$.end").value(end))
                .andExpect(jsonPath("$.booker.id").value(bookerId))
                .andExpect(jsonPath("$.item.id").value(itemId))
                .andExpect(jsonPath("$.item.name").value("Drill"))
                .andReturn();
        int bookingId = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(patch("/bookings/" + bookingId)
                        .header(USER_HEADER, bookerId)
                        .param("approved", "true"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/bookings/" + bookingId).header(USER_HEADER, bookerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WAITING"));
        mockMvc.perform(get("/bookings/" + bookingId).header(USER_HEADER, ownerId))
                .andExpect(status().isOk());
        mockMvc.perform(get("/bookings/" + bookingId).header(USER_HEADER, 9999))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/bookings").header(USER_HEADER, bookerId).param("state", "FUTURE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingId));
        mockMvc.perform(get("/bookings").header(USER_HEADER, bookerId).param("state", "UNKNOWN"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/bookings/owner").header(USER_HEADER, 9999))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/bookings/owner").header(USER_HEADER, ownerId).param("state", "WAITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingId));

        mockMvc.perform(patch("/bookings/" + bookingId)
                        .header(USER_HEADER, ownerId)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(post("/items/" + itemId + "/comment")
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Too early\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/items/" + itemId).header(USER_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nextBooking.id").value(bookingId))
                .andExpect(jsonPath("$.nextBooking.bookerId").value(bookerId))
                .andExpect(jsonPath("$.lastBooking").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.comments.length()").value(0));
        mockMvc.perform(get("/items").header(USER_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nextBooking.id").value(bookingId));

        String pastStart = LocalDateTime.now().plusSeconds(1).format(FORMATTER);
        String pastEnd = LocalDateTime.now().plusSeconds(2).format(FORMATTER);
        MvcResult pastCreated = mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(itemId, pastStart, pastEnd)))
                .andExpect(status().isOk())
                .andReturn();
        int pastId = JsonPath.read(pastCreated.getResponse().getContentAsString(), "$.id");
        mockMvc.perform(patch("/bookings/" + pastId)
                        .header(USER_HEADER, ownerId)
                        .param("approved", "true"))
                .andExpect(status().isOk());

        Thread.sleep(3000);
        mockMvc.perform(post("/items/" + itemId + "/comment")
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Great drill\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Great drill"))
                .andExpect(jsonPath("$.authorName").value("Booker"))
                .andExpect(jsonPath("$.created").exists());
        mockMvc.perform(get("/items/" + itemId).header(USER_HEADER, bookerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastBooking").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.nextBooking").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.comments.length()").value(1))
                .andExpect(jsonPath("$.comments[0].authorName").value("Booker"));
        mockMvc.perform(get("/items/" + itemId).header(USER_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastBooking.id").value(pastId))
                .andExpect(jsonPath("$.comments.length()").value(1));
    }

    private long createUser(String name, String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"email\":\"" + email + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private long createItem(long ownerId, String name, String description, boolean available) throws Exception {
        MvcResult result = mockMvc.perform(post("/items")
                        .header(USER_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"description\":\"" + description
                                + "\",\"available\":" + available + "}"))
                .andExpect(status().isOk())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private String bookingJson(long itemId, String start, String end) {
        return "{\"itemId\":" + itemId + ",\"start\":\"" + start + "\",\"end\":\"" + end + "\"}";
    }
}
