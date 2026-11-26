package ua.hotel.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.hotel.exception.DuplicateIdentityException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Thread-safe generic in-memory repository for storing domain entities by their unique identity.
 *
 * @param <T> the type of elements maintained by this repository
 */
public class GenericRepository<T> {

    private static final Logger log = LoggerFactory.getLogger(GenericRepository.class);

    private final String name;
    private final IdentityExtractor<T> extractor;
    private final Map<String, T> storage = new LinkedHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public GenericRepository(String name, IdentityExtractor<T> extractor) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Repository name must not be null or blank");
        }
        if (extractor == null) {
            throw new IllegalArgumentException("IdentityExtractor must not be null");
        }
        this.name = name;
        this.extractor = extractor;
    }

    public String getName() {
        return name;
    }

    public String identityOf(T item) {
        return validateAndExtractId(item);
    }

    public void add(T item) {
        String id = validateAndExtractId(item);
        lock.writeLock().lock();
        try {
            if (storage.containsKey(id)) {
                log.error("[{}] Cannot add item: duplicate identity '{}'", name, id);
                throw new DuplicateIdentityException(name, id);
            }
            storage.put(id, item);
            log.info("[{}] Added item with identity '{}'", name, id);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean addIfAbsent(T item) {
        String id = validateAndExtractId(item);
        lock.writeLock().lock();
        try {
            if (storage.containsKey(id)) {
                log.warn("[{}] Ignored duplicate item with identity '{}'", name, id);
                return false;
            }
            storage.put(id, item);
            log.info("[{}] Added item (ifAbsent) with identity '{}'", name, id);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean replace(T item) {
        String id = validateAndExtractId(item);
        lock.writeLock().lock();
        try {
            if (!storage.containsKey(id)) {
                log.warn("[{}] Cannot replace: item with identity '{}' not found", name, id);
                return false;
            }
            storage.put(id, item);
            log.info("[{}] Replaced item with identity '{}'", name, id);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addAll(Collection<? extends T> items) {
        if (items == null) {
            log.error("[{}] Cannot addAll from a null collection", name);
            throw new IllegalArgumentException("Items collection must not be null");
        }
        lock.writeLock().lock();
        try {
            for (T item : items) {
                add(item);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean removeByIdentity(String identity) {
        validateIdentityString(identity);
        lock.writeLock().lock();
        try {
            T removed = storage.remove(identity);
            if (removed != null) {
                log.info("[{}] Removed item with identity '{}'", name, identity);
                return true;
            } else {
                log.warn("[{}] Cannot remove: item with identity '{}' not found", name, identity);
                return false;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<T> findByIdentity(String identity) {
        validateIdentityString(identity);
        lock.readLock().lock();
        try {
            T item = storage.get(identity);
            if (item != null) {
                log.info("[{}] Found item with identity '{}'", name, identity);
                return Optional.of(item);
            } else {
                log.warn("[{}] Item with identity '{}' not found", name, identity);
                return Optional.empty();
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<T> findAll() {
        lock.readLock().lock();
        try {
            return List.copyOf(storage.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    public int size() {
        lock.readLock().lock();
        try {
            return storage.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    
    public Stream<T> stream() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(storage.values()).stream();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<T> findAllSorted(Comparator<T> comparator) {
        if (comparator == null) {
            throw new IllegalArgumentException("Comparator must not be null");
        }
        log.info("[{}] Sorting all elements using custom comparator", name);
        return stream()
                .sorted(comparator)
                .collect(Collectors.toUnmodifiableList());
    }

    public List<T> findAllSortedByIdentity(SortOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("SortOrder must not be null");
        }
        log.info("[{}] Sorting all elements by identity with order: {}", name, order);
        Comparator<T> comparator = Comparator.comparing(extractor::identityOf);
        if (order == SortOrder.DESC) {
            comparator = comparator.reversed();
        }
        return stream()
                .sorted(comparator)
                .collect(Collectors.toUnmodifiableList());
    }

    private String validateAndExtractId(T item) {
        if (item == null) {
            log.error("[{}] Attempted to add null item", name);
            throw new IllegalArgumentException("Item must not be null");
        }
        String id = extractor.identityOf(item);
        if (id == null || id.isBlank()) {
            log.error("[{}] Extracted identity is null or blank", name);
            throw new IllegalArgumentException("Item identity must not be null or blank");
        }
        return id;
    }

    private void validateIdentityString(String identity) {
        if (identity == null || identity.isBlank()) {
            log.error("[{}] Provided identity is null or blank", name);
            throw new IllegalArgumentException("Identity must not be null or blank");
        }
    }
}
