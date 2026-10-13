package ru.practicum.shareit.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class BaseClientTest {

    @Test
    void errorBodyIsJson() {
        RestTemplate rest = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(rest).build();
        server.expect(request -> assertThat(request.getURI().getPath()).isEqualTo("/requests"))
                .andRespond(org.springframework.test.web.client.response.MockRestResponseCreators
                        .withStatus(HttpStatus.CONFLICT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"conflict\"}"));

        TestClient client = new TestClient(rest);
        ResponseEntity<Object> response = client.post("/requests", 1L, Map.of("description", "need"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(new String((byte[]) response.getBody(), StandardCharsets.UTF_8)).isEqualTo("{\"error\":\"conflict\"}");
        server.verify();
    }

    @Test
    void successBodyStaysObject() {
        RestTemplate rest = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(rest).build();
        server.expect(request -> assertThat(request.getMethod()).isEqualTo(HttpMethod.GET))
                .andRespond(org.springframework.test.web.client.response.MockRestResponseCreators
                        .withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        TestClient client = new TestClient(rest);
        ResponseEntity<Object> response = client.get("/requests/1", 2L);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isInstanceOf(Map.class);
        server.verify();
    }

    private static final class TestClient extends BaseClient {
        private TestClient(RestTemplate rest) {
            super(rest);
        }
    }
}
