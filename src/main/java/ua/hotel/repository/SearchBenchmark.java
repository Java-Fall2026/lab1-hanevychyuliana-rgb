package ua.hotel.repository;

import ua.hotel.model.Guest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Benchmark comparing lookup performance between Map-backed GenericRepository and linear search over List.
 */
public class SearchBenchmark {

    private static final int TOTAL_ELEMENTS = 100_000;
    private static final int WARMUP_RUNS = 10;
    private static final int MEASURED_RUNS = 21;

    public static void main(String[] args) {
        IdentityExtractor<Guest> extractor = Guest::passportNumber;
        GenericRepository<Guest> repository = new GenericRepository<>("BenchmarkGuestRepo", extractor);

        LocalDate birthDate = LocalDate.of(1998, 5, 15);
        LocalDateTime createdAt = LocalDateTime.now();

        System.out.println("Populating repository with " + TOTAL_ELEMENTS + " elements...");
        for (int i = 1; i <= TOTAL_ELEMENTS; i++) {
            String passport = "PASS-" + i;
            repository.add(new Guest(passport, "FirstName", "LastName", birthDate, createdAt));
        }

        // Obtain the list ONCE before measurements as required by the specification
        List<Guest> itemsList = repository.findAll();

        String firstId = "PASS-1";
        String lastId = "PASS-" + TOTAL_ELEMENTS;
        String missingId = "PASS-999999";

        System.out.println("Running JVM warm-up...");
        for (int i = 0; i < WARMUP_RUNS; i++) {
            repository.findByIdentity(firstId);
            repository.findByIdentity(lastId);
            repository.findByIdentity(missingId);
            linearSearch(itemsList, firstId, extractor);
            linearSearch(itemsList, lastId, extractor);
            linearSearch(itemsList, missingId, extractor);
        }

        System.out.println("\n=== Search Benchmark (100,000 elements, median of " + MEASURED_RUNS + " runs in ns) ===");
        measureScenario("First element (" + firstId + ")", repository, itemsList, extractor, firstId);
        measureScenario("Last element (" + lastId + ")", repository, itemsList, extractor, lastId);
        measureScenario("Missing element (" + missingId + ")", repository, itemsList, extractor, missingId);
    }

    private static void measureScenario(
            String scenarioName,
            GenericRepository<Guest> repository,
            List<Guest> itemsList,
            IdentityExtractor<Guest> extractor,
            String targetIdentity) {

        long[] mapTimes = new long[MEASURED_RUNS];
        long[] listTimes = new long[MEASURED_RUNS];

        for (int i = 0; i < MEASURED_RUNS; i++) {
            long startMap = System.nanoTime();
            Optional<Guest> mapResult = repository.findByIdentity(targetIdentity);
            mapTimes[i] = System.nanoTime() - startMap;

            long startList = System.nanoTime();
            Optional<Guest> listResult = linearSearch(itemsList, targetIdentity, extractor);
            listTimes[i] = System.nanoTime() - startList;

            if (mapResult.isPresent() != listResult.isPresent()) {
                throw new IllegalStateException("Search results do not match!");
            }
        }

        Arrays.sort(mapTimes);
        Arrays.sort(listTimes);
        long medianMap = mapTimes[MEASURED_RUNS / 2];
        long medianList = listTimes[MEASURED_RUNS / 2];

        System.out.printf("%-30s | Map: %8d ns | List: %10d ns%n", scenarioName, medianMap, medianList);
    }

    private static <E> Optional<E> linearSearch(
            List<E> items, String identity, IdentityExtractor<E> extractor) {
        for (E item : items) {
            if (identity.equals(extractor.identityOf(item))) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }
}
