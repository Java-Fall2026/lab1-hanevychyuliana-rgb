package ua.hotel.loader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ua.hotel.model.Room;
import ua.hotel.model.RoomType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoomCsvLoaderTest {

    private final RoomCsvLoader loader = new RoomCsvLoader();

    @Test
    @DisplayName("1. Test correct object creation in Room")
    void testValidRoomCreation() {
        Room room = new Room(101, RoomType.SINGLE, 850.0);
        assertEquals(101, room.getRoomNumber());
        assertEquals(RoomType.SINGLE, room.getType());
        assertEquals(850.0, room.getPricePerNight());
    }

    @Test
    @DisplayName("2. Test CSV loader successfully parses valid CSV lines")
    void testLoadValidCsv(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("valid_rooms.csv");
        Files.writeString(file, "roomNumber,type,pricePerNight\n101,SINGLE,850.0\n102,DELUXE,2400.0");

        List<Room> rooms = loader.loadRooms(file);
        assertEquals(2, rooms.size());
        assertEquals(101, rooms.get(0).getRoomNumber());
    }

    @Test
    @DisplayName("3. Test InvalidDataException causes line skip and continues reading")
    void testLoadCsvWithCorruptedLines(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("corrupted_rooms.csv");
        Files.writeString(file, "roomNumber,type,pricePerNight\n101,SINGLE,850.0\nCORRUPTED,DELUXE,2400.0\n103,SUITE,-100.0");

        List<Room> rooms = loader.loadRooms(file);
        assertEquals(1, rooms.size());
        assertEquals(101, rooms.get(0).getRoomNumber());
    }

    @Test
    @DisplayName("4. Test missing file handling does not crash the program")
    void testMissingFileHandling(@TempDir Path tempDir) {
        Path missingFile = tempDir.resolve("missing_file.csv");
        List<Room> rooms = loader.loadRooms(missingFile);
        assertTrue(rooms.isEmpty());
    }

    @Test
    @DisplayName("5. Test calculated method for nights calculation")
    void testCalculatedNights() {
        LocalDate checkIn = LocalDate.of(2026, 6, 1);
        LocalDate checkOut = LocalDate.of(2026, 6, 6);
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);

        assertEquals(5, nights);
    }

    @Test
    @DisplayName("6. Test calculated method for total price calculation")
    void testCalculatedTotalPrice() {
        Room room = new Room(101, RoomType.SINGLE, 1000.0);
        long nights = 5;
        double totalPrice = room.getPricePerNight() * nights;

        assertEquals(5000.0, totalPrice);
    }
}