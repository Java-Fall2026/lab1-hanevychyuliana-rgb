package ua.hotel.loader;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ua.hotel.model.Booking;
import ua.hotel.model.BookingStatus;
import ua.hotel.model.Guest;
import ua.hotel.model.Room;
import ua.hotel.model.RoomType;
import ua.hotel.repository.FileStorage;
import ua.hotel.repository.GuestRepository;
import ua.hotel.repository.LoadResult;
import ua.hotel.repository.RoomRepository;
import ua.hotel.util.AppConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class Lab6Tests {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("1. JSON save and load produces equal list")
    void testJsonSaveAndLoad(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("rooms.json");
        FileStorage<Room> storage = new FileStorage<>(Room.class);
        List<Room> original = List.of(
                new Room(101, RoomType.SINGLE, 900.0),
                new Room(102, RoomType.SUITE, 2400.0)
        );

        storage.save(file, original);
        List<Room> loaded = storage.load(file);

        assertEquals(original, loaded);
    }

    @Test
    @DisplayName("2. YAML save and load produces equal list")
    void testYamlSaveAndLoad(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("guests.yaml");
        FileStorage<Guest> storage = new FileStorage<>(Guest.class);
        Guest guest = new Guest("AA123456", "Yuliana", "Hanevych", LocalDate.of(2004, 5, 15), null);
        List<Guest> original = List.of(guest);

        storage.save(file, original);
        List<Guest> loaded = storage.load(file);

        assertEquals(1, loaded.size());
        assertEquals(guest.passportNumber(), loaded.get(0).passportNumber());
        assertEquals(guest.birthDate(), loaded.get(0).birthDate());
    }

    @Test
    @DisplayName("3. Dates in saved file are serialized as ISO strings")
    void testDateSavedAsIsoString(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("guests.json");
        FileStorage<Guest> storage = new FileStorage<>(Guest.class);
        Guest guest = new Guest("AA123456", "Yuliana", "Hanevych", LocalDate.of(2004, 5, 15), null);

        storage.save(file, List.of(guest));
        String rawContent = Files.readString(file);

        assertTrue(rawContent.contains("\"2004-05-15\""));
        assertFalse(rawContent.contains("[ 2004, 5, 15 ]"));
    }

    @Test
    @DisplayName("4. Unknown file extension throws IllegalArgumentException")
    void testUnknownExtension(@TempDir Path tempDir) {
        Path file = tempDir.resolve("data.xml");
        FileStorage<Room> storage = new FileStorage<>(Room.class);

        assertThrows(IllegalArgumentException.class, () -> storage.load(file));
    }

    static Stream<Arguments> invalidEntitiesProvider() {
        return Stream.of(
                Arguments.of(new Room(-5, RoomType.SINGLE, 500.0), "roomNumber"),
                Arguments.of(new Room(101, null, 500.0), "type"),
                Arguments.of(new Room(101, RoomType.SINGLE, 5.0), "pricePerNight"),
                Arguments.of(new Guest("BAD", "Yuliana", "Hanevych", LocalDate.of(2000, 1, 1), null), "passportNumber"),
                Arguments.of(new Guest("AA123456", "", "Hanevych", LocalDate.of(2000, 1, 1), null), "firstName"),
                Arguments.of(new Guest("AA123456", "Yuliana", "Hanevych", LocalDate.of(2030, 1, 1), null), "birthDate")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidEntitiesProvider")
    @DisplayName("5. Standard validation annotations catch invalid fields")
    void testStandardValidationAnnotations(Object invalidEntity, String expectedField) {
        Set<ConstraintViolation<Object>> violations = validator.validate(invalidEntity);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(expectedField)));
    }

    @Test
    @DisplayName("6. Custom cross-field annotation ValidBookingDates validates check-out after check-in")
    void testCustomBookingDatesAnnotation() {
        Room room = new Room(101, RoomType.SINGLE, 900.0);
        Guest guest = new Guest("AA123456", "Yuliana", "Hanevych", LocalDate.of(2004, 5, 15), null);

        Booking validBooking = new Booking(
                room, guest, LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 15), BookingStatus.CONFIRMED
        );
        Booking invalidBooking = new Booking(
                room, guest, LocalDate.of(2026, 7, 15), LocalDate.of(2026, 7, 10), BookingStatus.CONFIRMED
        );

        assertTrue(validator.validate(validBooking).isEmpty());
        assertFalse(validator.validate(invalidBooking).isEmpty());
    }

    @Test
    @DisplayName("7. DataLoader adds only valid records and records all rejections")
    void testDataLoaderFiltersInvalidRecords(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("rooms.json");
        Files.writeString(file, """
                [
                  {"roomNumber": 101, "type": "SINGLE", "pricePerNight": 900.0},
                  {"roomNumber": -5, "type": "SINGLE", "pricePerNight": -10.0}
                ]
                """);

        FileStorage<Room> storage = new FileStorage<>(Room.class);
        RoomRepository repo = new RoomRepository();
        DataLoader<Room> loader = new DataLoader<>();

        LoadResult result = loader.loadData(file, repo, storage);

        assertEquals(2, result.readCount());
        assertEquals(1, result.addedCount());
        assertEquals(1, result.rejectedRecords().size());
        assertTrue(result.rejectedRecords().get(0).contains("roomNumber"));
        assertTrue(result.rejectedRecords().get(0).contains("pricePerNight"));
    }

    @Test
    @DisplayName("8. Duplicate identity is rejected gracefully without throwing exception")
    void testDuplicateIdentityRejected(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("guests.yaml");
        Files.writeString(file, """
                - passportNumber: "AA123456"
                  firstName: "Yuliana"
                  lastName: "Hanevych"
                  birthDate: "2004-05-15"
                - passportNumber: "AA123456"
                  firstName: "Duplicate"
                  lastName: "User"
                  birthDate: "1995-01-01"
                """);

        FileStorage<Guest> storage = new FileStorage<>(Guest.class);
        GuestRepository repo = new GuestRepository();
        DataLoader<Guest> loader = new DataLoader<>();

        LoadResult result = loader.loadData(file, repo, storage);

        assertEquals(2, result.readCount());
        assertEquals(1, result.addedCount());
        assertEquals(1, result.rejectedRecords().size());
        assertTrue(result.rejectedRecords().get(0).toLowerCase().contains("duplicate"));
    }

    @Test
    @DisplayName("9. Syntactically broken JSON file does not crash DataLoader")
    void testBrokenJsonFileDoesNotCrash(@TempDir Path tempDir) throws IOException {
        Path brokenFile = tempDir.resolve("broken.json");
        Files.writeString(brokenFile, "[{\"roomNumber\": 101, BROKEN");

        FileStorage<Room> storage = new FileStorage<>(Room.class);
        RoomRepository repo = new RoomRepository();
        DataLoader<Room> loader = new DataLoader<>();

        LoadResult result = loader.loadData(brokenFile, repo, storage);

        assertEquals(0, result.readCount());
        assertEquals(0, result.addedCount());
        assertEquals(1, result.rejectedRecords().size());
    }

    @Test
    @DisplayName("10. Non-existent file does not crash DataLoader")
    void testMissingFileDoesNotCrash(@TempDir Path tempDir) {
        Path missingFile = tempDir.resolve("does_not_exist.json");

        FileStorage<Room> storage = new FileStorage<>(Room.class);
        RoomRepository repo = new RoomRepository();
        DataLoader<Room> loader = new DataLoader<>();

        LoadResult result = loader.loadData(missingFile, repo, storage);

        assertEquals(0, result.readCount());
        assertEquals(0, result.addedCount());
        assertEquals(1, result.rejectedRecords().size());
    }

    @Test
    @DisplayName("11. Missing key in Properties throws exception containing the key name")
    void testMissingPropertyKeyThrowsException() {
        Properties props = new Properties();
        props.setProperty("rooms.file", "data/rooms.json");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> AppConfig.getProperty(props, "missing.custom.key")
        );

        assertTrue(ex.getMessage().contains("missing.custom.key"));
    }
}
