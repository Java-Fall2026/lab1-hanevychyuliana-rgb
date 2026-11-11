package ua.hotel.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.hotel.model.Room;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public class RoomRepository extends GenericRepository<Room> {

    private static final Logger log = LoggerFactory.getLogger(RoomRepository.class);

       public RoomRepository() {
        super("rooms", room -> String.valueOf(room.getRoomNumber()));
    }


    public RoomRepository(IdentityExtractor<Room> extractor) {
        super("rooms", extractor);
    }

    public List<Room> findAllSortedByPriceThenNumber() {
        log.info("[rooms] Sorting rooms by price (ascending) then by room number");
        Comparator<Room> comparator = Comparator
                .comparingDouble(Room::getPricePerNight)
                .thenComparingInt(Room::getRoomNumber);
        return findAllSorted(comparator);
    }

       public List<Room> findAllSortedByTypeAndPriceDesc() {
        log.info("[rooms] Sorting rooms by type (alphabetically) then by price (descending)");
        Comparator<Room> comparator = Comparator
                .comparing((Room room) -> room.getType().name())
                .thenComparing(Room::getPricePerNight, Comparator.reverseOrder());
        return findAllSorted(comparator);
    }

    public List<Room> searchByType(String typeQuery) {
        if (typeQuery == null || typeQuery.isBlank()) return List.of();
        String upper = typeQuery.trim().toUpperCase();
        log.info("[rooms] Searching rooms by type containing: '{}'", upper);
        return stream()
                .filter(r -> r.getType().name().contains(upper))
                .collect(Collectors.toUnmodifiableList());
    }

    public List<Room> searchByPriceRange(double minPrice, double maxPrice) {
        log.info("[rooms] Searching rooms by price range: [{}, {}]", minPrice, maxPrice);
        return stream()
                .filter(r -> r.getPricePerNight() >= minPrice && r.getPricePerNight() <= maxPrice)
                .sorted()
                .collect(Collectors.toUnmodifiableList());
    }

    public List<Room> searchByTypeAndMaxPrice(String type, double maxPrice) {
        log.info("[rooms] Searching rooms by type '{}' and max price {}", type, maxPrice);
        return stream()
                .filter(r -> r.getType().name().equalsIgnoreCase(type))
                .filter(r -> r.getPricePerNight() <= maxPrice)
                .collect(Collectors.toUnmodifiableList());
    }

    public Optional<Room> findFirstCheapestRoom() {
        log.info("[rooms] Finding the single cheapest room");
        return stream()
                .sorted(Comparator.comparingDouble(Room::getPricePerNight))
                .findFirst();
    }

    public Map<ua.hotel.model.RoomType, List<Room>> groupRoomsByType() {
        log.info("[rooms] Grouping rooms by room type");
        return stream().collect(Collectors.groupingBy(Room::getType));
    }

    public double getAveragePrice() {
        log.info("[rooms] Calculating average room price");
        return stream()
                .mapToDouble(Room::getPricePerNight)
                .average()
                .orElse(0.0);
    }
}
