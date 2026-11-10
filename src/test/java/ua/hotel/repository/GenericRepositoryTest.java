package ua.hotel.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ua.hotel.exception.DuplicateIdentityException;
import ua.hotel.model.Booking;
import ua.hotel.model.BookingStatus;
import ua.hotel.model.Guest;
import ua.hotel.model.HotelPayment;
import ua.hotel.model.Room;
import ua.hotel.model.RoomType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GenericRepositoryTest {

    private GenericRepository<Room> roomRepo;
    private GenericRepository<Guest> guestRepo;
    private GenericRepository<HotelPayment> paymentRepo;

    @BeforeEach
    void setUp() {
        roomRepo = new GenericRepository<>(
                "RoomRepository",
                room -> String.valueOf(room.getRoomNumber())
        );
        guestRepo = new GenericRepository<>(
                "GuestRepository",
                Guest::passportNumber
        );
        paymentRepo = new GenericRepository<>(
                "PaymentRepository",
                HotelPayment::paymentId
        );
    }

    @Test
    @DisplayName("1. add and findByIdentity should find the added element")
    void shouldAddAndFindElementByIdentity() {
        Room room = new Room(101, RoomType.SINGLE, 850.0);
        roomRepo.add(room);

        Optional<Room> found = roomRepo.findByIdentity("101");

        assertTrue(found.isPresent());
        assertEquals(room, found.get());
        assertEquals(1, roomRepo.size());
    }

    @Test
    @DisplayName("2. findByIdentity on missing identity should return empty Optional")
    void shouldReturnEmptyOptionalWhenIdentityDoesNotExist() {
        Optional<Room> found = roomRepo.findByIdentity("999");

        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("3. repeated add with existing identity should throw DuplicateIdentityException")
    void shouldThrowDuplicateIdentityExceptionOnDuplicateAdd() {
        Room room1 = new Room(101, RoomType.SINGLE, 850.0);
        Room room2 = new Room(101, RoomType.DELUXE, 2400.0);
        roomRepo.add(room1);

        DuplicateIdentityException ex = assertThrows(
                DuplicateIdentityException.class,
                () -> roomRepo.add(room2)
        );

        assertTrue(ex.getMessage().contains("101"));
        assertTrue(ex.getMessage().contains("RoomRepository"));
        assertEquals(1, roomRepo.size());
    }

    @Test
    @DisplayName("4. addIfAbsent should return false on duplicate and not change size")
    void shouldReturnFalseAndNotChangeSizeOnAddIfAbsentDuplicate() {
        Room room1 = new Room(101, RoomType.SINGLE, 850.0);
        Room roomDuplicate = new Room(101, RoomType.SUITE, 1800.0);

        assertTrue(roomRepo.addIfAbsent(room1));
        assertFalse(roomRepo.addIfAbsent(roomDuplicate));
        assertEquals(1, roomRepo.size());
    }

    @Test
    @DisplayName("5. removeByIdentity should return true for existing and false for missing element")
    void shouldRemoveExistingAndReturnFalseForMissingIdentity() {
        Room room = new Room(105, RoomType.DOUBLE, 1200.0);
        roomRepo.add(room);

        assertTrue(roomRepo.removeByIdentity("105"));
        assertEquals(0, roomRepo.size());
        assertFalse(roomRepo.removeByIdentity("105"));
        assertFalse(roomRepo.removeByIdentity("404"));
    }

    @Test
    @DisplayName("6. attempting to modify findAll result should not alter repository state")
    void shouldNotAllowModifyingRepositoryViaFindAllList() {
        roomRepo.add(new Room(101, RoomType.SINGLE, 850.0));
        List<Room> allRooms = roomRepo.findAll();

        assertThrows(UnsupportedOperationException.class, allRooms::clear);
        assertEquals(1, roomRepo.size());
    }

    @Test
    @DisplayName("7. same GenericRepository class should work with three different domain types")
    void shouldWorkWithThreeDifferentDomainTypes() {
        Room room = new Room(301, RoomType.SUITE, 2200.0);
        Guest guest = new Guest("AA123456", "Yuliana", "Hanevych", LocalDate.of(2004, 5, 15), null);
        Booking booking = new Booking(
                room,
                guest,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 15),
                BookingStatus.CONFIRMED
        );
        HotelPayment payment = new HotelPayment("PAY-9001", booking, 11000.0, LocalDate.of(2026, 7, 10), null);

        roomRepo.add(room);
        guestRepo.add(guest);
        paymentRepo.add(payment);

        assertEquals(Optional.of(room), roomRepo.findByIdentity("301"));
        assertEquals(Optional.of(guest), guestRepo.findByIdentity("AA123456"));
        assertEquals(Optional.of(payment), paymentRepo.findByIdentity("PAY-9001"));
    }

    @Test
    @DisplayName("8. add(null) or blank identity should throw IllegalArgumentException")
    void shouldThrowIllegalArgumentExceptionOnNullOrBlankInputs() {
        assertThrows(IllegalArgumentException.class, () -> roomRepo.add(null));
        assertThrows(IllegalArgumentException.class, () -> roomRepo.addIfAbsent(null));
        assertThrows(IllegalArgumentException.class, () -> roomRepo.findByIdentity("   "));
        assertThrows(IllegalArgumentException.class, () -> roomRepo.removeByIdentity(null));
    }

    @Test
    @DisplayName("9. addAll should add all elements from a collection")
    void shouldAddAllElementsFromCollection() {
        List<Room> rooms = List.of(
                new Room(101, RoomType.SINGLE, 850.0),
                new Room(102, RoomType.DOUBLE, 1400.0)
        );

        roomRepo.addAll(rooms);

        assertEquals(2, roomRepo.size());
        assertTrue(roomRepo.findByIdentity("102").isPresent());
    }
}
