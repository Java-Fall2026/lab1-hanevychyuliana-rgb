package ua.hotel.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ua.hotel.model.Guest;
import ua.hotel.model.Room;
import ua.hotel.model.RoomType;
import ua.hotel.repository.GuestRepository;
import ua.hotel.repository.RoomRepository;
import ua.hotel.util.ObjectMapperFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class RestApiIntegrationTest {

    private ApiServer server;
    private RoomRepository roomRepo;
    private GuestRepository guestRepo;
    private HttpClient client;
    private ObjectMapper mapper;
    private String baseUrl;

    @BeforeEach
    void setUp() throws Exception {
        mapper = ObjectMapperFactory.create();
        roomRepo = new RoomRepository();
        guestRepo = new GuestRepository();

        roomRepo.add(new Room(101, RoomType.SINGLE, 900.0));
        roomRepo.add(new Room(102, RoomType.DOUBLE, 1500.0));
        roomRepo.add(new Room(103, RoomType.SUITE, 2800.0));

        guestRepo.add(new Guest("AA123456", "Юліана", "Ганевич", LocalDate.of(2004, 5, 15), null));

        server = new ApiServer(0, roomRepo, guestRepo, mapper);
        server.start();
        baseUrl = "http://localhost:" + server.port();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    @DisplayName("1. GET collection returns 200, expected array size, and application/json UTF-8 Content-Type")
    void shouldGetRoomsCollection() throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + "/api/rooms")).GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode());
        String contentType = resp.headers().firstValue("Content-Type").orElse("");
        assertTrue(contentType.contains("application/json"));
        assertTrue(contentType.toLowerCase().contains("charset=utf-8"));

        List<Room> rooms = mapper.readValue(resp.body(), new TypeReference<>() {});
        assertEquals(3, rooms.size());
    }

    @Test
    @DisplayName("2. GET by existing identity returns 200 and missing identity returns 404 with ApiError")
    void shouldGetRoomByIdAndReturn404WhenMissing() throws Exception {
        HttpRequest okReq = HttpRequest.newBuilder(URI.create(baseUrl + "/api/rooms/101")).GET().build();
        HttpResponse<String> okResp = client.send(okReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(200, okResp.statusCode());
        Room room = mapper.readValue(okResp.body(), Room.class);
        assertEquals(101, room.getRoomNumber());

        HttpRequest notFoundReq = HttpRequest.newBuilder(URI.create(baseUrl + "/api/rooms/999")).GET().build();
        HttpResponse<String> notFoundResp = client.send(notFoundReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(404, notFoundResp.statusCode());
        ApiError error = mapper.readValue(notFoundResp.body(), ApiError.class);
        assertEquals(404, error.status());
        assertEquals("Not Found", error.error());
        assertTrue(error.violations().isEmpty());
    }

    @Test
    @DisplayName("3. POST valid object returns 201, Location header, and GET at Location returns created object")
    void shouldCreateRoomAndReturnLocationHeader() throws Exception {
        String body = """
                {"roomNumber": 205, "type": "DELUXE", "pricePerNight": 3200.0}
                """;
        HttpRequest postReq = HttpRequest.newBuilder(URI.create(baseUrl + "/api/rooms"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> postResp = client.send(postReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(201, postResp.statusCode());

        String location = postResp.headers().firstValue("Location").orElseThrow();
        assertEquals("/api/rooms/205", location);

        HttpRequest getReq = HttpRequest.newBuilder(URI.create(baseUrl + location)).GET().build();
        HttpResponse<String> getResp = client.send(getReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(200, getResp.statusCode());
        Room fetched = mapper.readValue(getResp.body(), Room.class);
        assertEquals(205, fetched.getRoomNumber());
        assertEquals(RoomType.DELUXE, fetched.getType());
    }

    @Test
    @DisplayName("4. POST invalid object returns 400 with all violations including custom annotation")
    void shouldReturn400WithAllViolationsForInvalidGuest() throws Exception {
        int initialSize = guestRepo.size();
        // Invalid passport (custom @ValidPassport) and blank firstName (@NotBlank)
        String invalidGuestJson = """
                {
                  "passportNumber": "invalid123",
                  "firstName": "   ",
                  "lastName": "Шевченко",
                  "birthDate": "1995-03-09"
                }
                """;
        HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + "/api/guests"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(invalidGuestJson, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(400, resp.statusCode());
        assertEquals(initialSize, guestRepo.size());

        ApiError error = mapper.readValue(resp.body(), ApiError.class);
        assertEquals(400, error.status());
        assertEquals(2, error.violations().size());
        assertTrue(error.violations().stream().anyMatch(v -> v.field().equals("passportNumber")));
        assertTrue(error.violations().stream().anyMatch(v -> v.field().equals("firstName")));
    }

    @Test
    @DisplayName("5. POST duplicate identity returns 409 Conflict without changing repository size")
    void shouldReturn409OnDuplicatePost() throws Exception {
        int initialSize = roomRepo.size();
        String duplicateJson = """
                {"roomNumber": 101, "type": "SINGLE", "pricePerNight": 950.0}
                """;
        HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + "/api/rooms"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(duplicateJson, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(409, resp.statusCode());
        assertEquals(initialSize, roomRepo.size());

        ApiError error = mapper.readValue(resp.body(), ApiError.class);
        assertEquals("Conflict", error.error());
    }

    @Test
    @DisplayName("6. POST syntactically malformed JSON returns 400 Bad Request, not 500")
    void shouldReturn400OnMalformedJson() throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + "/api/rooms"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{broken-json", StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(400, resp.statusCode());
    }

    @Test
    @DisplayName("7. POST with Content-Type text/plain returns 415 Unsupported Media Type")
    void shouldReturn415OnUnsupportedContentType() throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + "/api/rooms"))
                .header("Content-Type", "text/plain")
                .POST(HttpRequest.BodyPublishers.ofString("101,SINGLE,900", StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(415, resp.statusCode());
        ApiError error = mapper.readValue(resp.body(), ApiError.class);
        assertEquals("Unsupported Media Type", error.error());
    }

    @Test
    @DisplayName("8. PUT updates existing object (200), returns 404 when missing, and 400 on ID mismatch")
    void shouldHandlePutUpdateMissingAndIdMismatch() throws Exception {

        String updateJson = """
                {"roomNumber": 101, "type": "SINGLE", "pricePerNight": 1150.0}
                """;
        HttpRequest putOk = HttpRequest.newBuilder(URI.create(baseUrl + "/api/rooms/101"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(updateJson, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> putOkResp = client.send(putOk, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(200, putOkResp.statusCode());
        assertEquals(1150.0, roomRepo.findByIdentity("101").orElseThrow().getPricePerNight());

        String missingJson = """
                {"roomNumber": 777, "type": "SUITE", "pricePerNight": 2000.0}
                """;
        HttpRequest putMissing = HttpRequest.newBuilder(URI.create(baseUrl + "/api/rooms/777"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(missingJson, StandardCharsets.UTF_8))
                .build();
        assertEquals(404, client.send(putMissing, HttpResponse.BodyHandlers.ofString()).statusCode());

        HttpRequest putMismatch = HttpRequest.newBuilder(URI.create(baseUrl + "/api/rooms/102"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(updateJson, StandardCharsets.UTF_8))
                .build();
        assertEquals(400, client.send(putMismatch, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    @DisplayName("9. DELETE returns 204 No Content and subsequent GET returns 404")
    void shouldDeleteRoomAndReturn204Then404() throws Exception {
        HttpRequest delReq = HttpRequest.newBuilder(URI.create(baseUrl + "/api/rooms/103")).DELETE().build();
        HttpResponse<String> delResp = client.send(delReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(204, delResp.statusCode());
        assertTrue(delResp.body().isEmpty());

        HttpRequest getReq = HttpRequest.newBuilder(URI.create(baseUrl + "/api/rooms/103")).GET().build();
        assertEquals(404, client.send(getReq, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    @DisplayName("10. Unsupported HTTP method returns 405 Method Not Allowed in ApiError format")
    void shouldReturn405OnUnsupportedMethod() throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + "/api/rooms")).DELETE().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(405, resp.statusCode());
        ApiError error = mapper.readValue(resp.body(), ApiError.class);
        assertEquals(405, error.status());
        assertEquals("Method Not Allowed", error.error());
    }

    @Test
    @DisplayName("11. Sorting with order=ASC and order=DESC gives reversed lists; unknown sort returns 400")
    void shouldSortAscAndDescAndRejectUnknownSort() throws Exception {
        HttpRequest ascReq = HttpRequest.newBuilder(URI.create(baseUrl + "/api/rooms?sort=price&order=ASC")).GET().build();
        HttpRequest descReq = HttpRequest.newBuilder(URI.create(baseUrl + "/api/rooms?sort=price&order=DESC")).GET().build();

        List<Room> ascRooms = mapper.readValue(
                client.send(ascReq, HttpResponse.BodyHandlers.ofString()).body(), new TypeReference<>() {});
        List<Room> descRooms = mapper.readValue(
                client.send(descReq, HttpResponse.BodyHandlers.ofString()).body(), new TypeReference<>() {});

        List<Room> reversedDesc = new ArrayList<>(descRooms);
        Collections.reverse(reversedDesc);
        assertEquals(ascRooms, reversedDesc);

        HttpRequest badSortReq = HttpRequest.newBuilder(URI.create(baseUrl + "/api/rooms?sort=unknownField")).GET().build();
        assertEquals(400, client.send(badSortReq, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    @DisplayName("12. Price range filter and pagination return expected subset")
    void shouldFilterByRangeAndPaginate() throws Exception {
        HttpRequest req = HttpRequest.newBuilder(
                URI.create(baseUrl + "/api/rooms?minPrice=1000&maxPrice=3000&sort=price&order=ASC&offset=1&limit=1")
        ).GET().build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        List<Room> result = mapper.readValue(resp.body(), new TypeReference<>() {});
        assertEquals(1, result.size());
        assertEquals(103, result.get(0).getRoomNumber());
    }

    @Test
    @DisplayName("13. Ukrainian Cyrillic text survives POST and GET round-trip unchanged")
    void shouldPreserveUkrainianCyrillicText() throws Exception {
        String guestJson = """
                {
                  "passportNumber": "KC778899",
                  "firstName": "Соломія",
                  "lastName": "Коцюбинська-Їжакевич",
                  "birthDate": "1999-11-24"
                }
                """;
        HttpRequest postReq = HttpRequest.newBuilder(URI.create(baseUrl + "/api/guests"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(guestJson, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> postResp = client.send(postReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(201, postResp.statusCode());

        HttpRequest getReq = HttpRequest.newBuilder(URI.create(baseUrl + "/api/guests/KC778899")).GET().build();
        HttpResponse<String> getResp = client.send(getReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Guest fetched = mapper.readValue(getResp.body(), Guest.class);
        assertEquals("Соломія", fetched.firstName());
        assertEquals("Коцюбинська-Їжакевич", fetched.lastName());
    }

    @Test
    @DisplayName("14. Concurrent POST requests with identical identity produce exactly one 201 and rest 409")
    void shouldHandleConcurrentDuplicatePostsSafely() throws Exception {
        int concurrentRequests = 10;
        String payload = """
                {"roomNumber": 500, "type": "SUITE", "pricePerNight": 2500.0}
                """;

        List<CompletableFuture<HttpResponse<String>>> futures = new ArrayList<>();
        for (int i = 0; i < concurrentRequests; i++) {
            HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + "/api/rooms"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .build();
            futures.add(client.sendAsync(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)));
        }

        List<Integer> statuses = futures.stream()
                .map(CompletableFuture::join)
                .map(HttpResponse::statusCode)
                .toList();

        long createdCount = statuses.stream().filter(s -> s == 201).count();
        long conflictCount = statuses.stream().filter(s -> s == 409).count();

        assertEquals(1, createdCount);
        assertEquals(concurrentRequests - 1, conflictCount);
        assertTrue(roomRepo.findByIdentity("500").isPresent());
    }

    @Test
    @DisplayName("15. Stats endpoint returns aggregated statistics and unmapped route returns 404 ApiError")
    void shouldReturnStatsAndHandleUnmappedPath() throws Exception {
        HttpRequest statsReq = HttpRequest.newBuilder(URI.create(baseUrl + "/api/rooms/stats")).GET().build();
        HttpResponse<String> statsResp = client.send(statsReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(200, statsResp.statusCode());
        Map<String, Object> stats = mapper.readValue(statsResp.body(), new TypeReference<>() {});
        assertEquals(3, stats.get("totalRooms"));

        HttpRequest unknownReq = HttpRequest.newBuilder(URI.create(baseUrl + "/api/unknown")).GET().build();
        HttpResponse<String> unknownResp = client.send(unknownReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(404, unknownResp.statusCode());
        ApiError error = mapper.readValue(unknownResp.body(), ApiError.class);
        assertEquals(404, error.status());
    }
}
