package ua.hotel.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.hotel.model.Guest;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public class GuestRepository extends GenericRepository<Guest> {

    private static final Logger log = LoggerFactory.getLogger(GuestRepository.class);

    public GuestRepository() {
        super("guests", Guest::passportNumber); // Посилання на метод за умовою!
    }

    public List<Guest> findAllSortedByLastNameThenBirthDate() {
        log.info("[guests] Sorting guests by last name then birth date");
        Comparator<Guest> comparator = Comparator
                .comparing(Guest::lastName, Comparator.nullsLast(String::compareTo))
                .thenComparing(Guest::birthDate, Comparator.nullsLast(LocalDate::compareTo));
        return findAllSorted(comparator);
    }

    public List<Guest> searchByLastName(String query) {
        if (query == null || query.isBlank()) return List.of();
        String q = query.trim().toLowerCase();
        log.info("[guests] Searching guests by last name containing: '{}'", q);
        return stream()
                .filter(g -> g.lastName().toLowerCase().contains(q))
                .collect(Collectors.toUnmodifiableList());
    }

    public Optional<Guest> findByPassport(String passport) {
        log.info("[guests] Finding guest by passport: '{}'", passport);
        return stream()
                .filter(g -> g.passportNumber().equalsIgnoreCase(passport))
                .findFirst();
    }

    public Map<Integer, Long> countGuestsByBirthYear() {
        log.info("[guests] Counting guests grouped by birth year");
        return stream().collect(Collectors.groupingBy(
                g -> g.birthDate().getYear(),
                Collectors.counting()
        ));
    }
}
