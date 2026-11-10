package ua.hotel.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ua.hotel.model.Guest;
import ua.hotel.model.Room;
import ua.hotel.model.RoomType;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class RoomAndGuestRepositoryTest {

    private RoomRepository roomRepo;
    private GuestRepository guestRepo;

    @BeforeEach
    void setUp() {
        roomRepo = new RoomRepository();
        guestRepo = new GuestRepository();
    }

    @Test
    @DisplayName("1. Natural ordering via Comparable on list sorting")
    void testNaturalOrdering() {
        Room r1 = new Room(105, RoomType.SINGLE, 1000.0);
        Room r2 = new Room(101, RoomType.DOUBLE, 1200.0);
        List<Room> list = new java.util.ArrayList<>(List.of(r1, r2));
        
        Collections.sort(list);
        assertEquals(101, list.get(0).getRoomNumber());
    }

    @Test
    @DisplayName("2. Multilevel comparator sorts correctly by primary and secondary keys")
    void testMultilevelComparator() {
        Room r1 = new Room(102, RoomType.SINGLE, 1500.0);
        Room r2 = new Room(101, RoomType.SINGLE, 1500.0);
        Room r3 = new Room(103, RoomType.SINGLE, 1000.0);
        roomRepo.addAll(List.of(r1, r2, r3));

        List<Room> sorted = roomRepo.findAllSortedByPriceThenNumber();
        assertEquals(103, sorted.get(0).getRoomNumber()); // Найнижча ціна (1000)
        assertEquals(101, sorted.get(1).getRoomNumber()); // Ціна 1500, але номер менший
        assertEquals(102, sorted.get(2).getRoomNumber()); // Ціна 1500, номер більший
    }

    @Test
    @DisplayName("3. findAllSortedByIdentity in ASC and DESC give reverse orders")
    void testFindAllSortedByIdentityAscDesc() {
        Guest g1 = new Guest("AA111111", "Ann", "Smith", LocalDate.of(2000, 1, 1), null);
        Guest g2 = new Guest("ZZ999999", "Bob", "Brown", LocalDate.of(1995, 1, 1), null);
        guestRepo.addAll(List.of(g1, g2));

        List<Guest> asc = guestRepo.findAllSortedByIdentity(SortOrder.ASC);
        List<Guest> desc = guestRepo.findAllSortedByIdentity(SortOrder.DESC);

        assertEquals("AA111111", asc.get(0).passportNumber());
        assertEquals("ZZ999999", desc.get(0).passportNumber());
    }

    @Test
    @DisplayName("4. Rooms with fractional prices difference < 1 are sorted correctly")
    void testFractionalPricesSorting() {
        Room r1 = new Room(101, RoomType.SINGLE, 900.50);
        Room r2 = new Room(102, RoomType.SINGLE, 900.20);
        roomRepo.addAll(List.of(r1, r2));

        List<Room> sorted = roomRepo.findAllSortedByPriceThenNumber();
        assertEquals(900.20, sorted.get(0).getPricePerNight());
        assertEquals(900.50, sorted.get(1).getPricePerNight());
    }

    @Test
    @DisplayName("5. TreeSet with unaligned comparator behaves according to contract")
    void testTreeSetUnalignedComparator() {
        Comparator<Room> priceComparator = Comparator.comparingDouble(Room::getPricePerNight);
        Set<Room> set = new TreeSet<>(priceComparator);

        Room r1 = new Room(101, RoomType.SINGLE, 1000.0);
        Room r2 = new Room(105, RoomType.DOUBLE, 1000.0); // Різні номери, однакова ціна

        set.add(r1);
        set.add(r2);

        assertEquals(1, set.size(), "TreeSet treats elements as duplicates if comparator returns 0");
    }

    @Test
    @DisplayName("6. Search methods filter correctly by range and multiple conditions")
    void testSearchMethods() {
        Room r1 = new Room(101, RoomType.SINGLE, 800.0);
        Room r2 = new Room(102, RoomType.SUITE, 2500.0);
        roomRepo.addAll(List.of(r1, r2));

        List<Room> rangeResults = roomRepo.searchByPriceRange(500.0, 1000.0);
        assertEquals(1, rangeResults.size());
        assertEquals(101, rangeResults.get(0).getRoomNumber());

        List<Room> multiResults = roomRepo.searchByTypeAndMaxPrice("SUITE", 3000.0);
        assertEquals(1, multiResults.size());
        
        List<Room> emptyMulti = roomRepo.searchByTypeAndMaxPrice("SINGLE", 500.0);
        assertTrue(emptyMulti.isEmpty());
    }

    @Test
    @DisplayName("7. Single object search returns Optional.empty when missing")
    void testSingleObjectSearchOptionalEmpty() {
        Optional<Room> found = roomRepo.findFirstCheapestRoom();
        assertTrue(found.isEmpty());

        Optional<Guest> missingGuest = guestRepo.findByPassport("NONEXISTENT");
        assertTrue(missingGuest.isEmpty());
    }

    @Test
    @DisplayName("8. groupingBy produces correct group count and sizes")
    void testGroupingBy() {
        Room r1 = new Room(101, RoomType.SINGLE, 900.0);
        Room r2 = new Room(102, RoomType.SINGLE, 950.0);
        Room r3 = new Room(103, RoomType.SUITE, 2000.0);
        roomRepo.addAll(List.of(r1, r2, r3));

        Map<RoomType, List<Room>> groups = roomRepo.groupRoomsByType();
        assertEquals(2, groups.size());
        assertEquals(2, groups.get(RoomType.SINGLE).size());
    }

    @Test
    @DisplayName("9. Aggregation on empty collection returns safe default value")
    void testEmptyCollectionAggregation() {
        double avg = roomRepo.getAveragePrice();
        assertEquals(0.0, avg);
    }

    @Test
    @DisplayName("10. Reusing consumed stream throws IllegalStateException")
    void testStreamReuseThrowsException() {
        roomRepo.add(new Room(101, RoomType.SINGLE, 900.0));
        Stream<Room> stream = roomRepo.stream();
        stream.count(); // Consume stream

        assertThrows(IllegalStateException.class, stream::count);
    }
}
