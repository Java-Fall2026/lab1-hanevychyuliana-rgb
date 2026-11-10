package ua.hotel.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.hotel.exception.DuplicateIdentityException;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Generic in-memory repository for storing domain entities by their unique identity.
 *
 * @param <T> the type of elements maintained by this repository
 */
public class GenericRepository<T> {

    private static final Logger log = LoggerFactory.getLogger(GenericRepository.class);

    private final String name;
    private final IdentityExtractor<T> extractor;
    private final Map<String, T> storage = new LinkedHashMap<>();

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

    public void add(T item) {
        String id = validateAndExtractId(item);
        if (storage.containsKey(id)) {
            log.error("[{}] Cannot add item: duplicate identity '{}'", name, id);
            throw new DuplicateIdentityException(name, id);
        }
        storage.put(id, item);
        log.info("[{}] Added item with identity '{}'", name, id);
    }

    /**
     * Adds an item only if its identity is not already present in the repository.
     *
     * @return true if added, false if duplicate was ignored
     */
    public boolean addIfAbsent(T item) {
        String id = validateAndExtractId(item);
        if (storage.containsKey(id)) {
            log.warn("[{}] Ignored duplicate item with identity '{}'", name, id);
            return false;
        }
        storage.put(id, item);
        log.info("[{}] Added item (ifAbsent) with identity '{}'", name, id);
        return true;
    }

    /**
     * Adds all items from the given collection using an upper-bounded wildcard.
     */
    public void addAll(Collection<? extends T> items) {
        if (items == null) {
            log.error("[{}] Cannot addAll from a null collection", name);
            throw new IllegalArgumentException("Items collection must not be null");
        }
        for (T item : items) {
            add(item);
        }
    }

    /**
     * Removes an element by its identity.
     *
     * @return true if removed, false if not found
     */
    public boolean removeByIdentity(String identity) {
        validateIdentityString(identity);
        T removed = storage.remove(identity);
        if (removed != null) {
            log.info("[{}] Removed item with identity '{}'", name, identity);
            return true;
        } else {
            log.warn("[{}] Cannot remove: item with identity '{}' not found", name, identity);
            return false;
        }
    }

    /**
     * Finds an element by its identity.
     */
    public Optional<T> findByIdentity(String identity) {
        validateIdentityString(identity);
        T item = storage.get(identity);
        if (item != null) {
            log.info("[{}] Found item with identity '{}'", name, identity);
            return Optional.of(item);
        } else {
            log.warn("[{}] Item with identity '{}' not found", name, identity);
            return Optional.empty();
        }
    }

    /**
     * Returns an unmodifiable copy of all elements in the repository.
     */
    public List<T> findAll() {
        return List.copyOf(storage.values());
    }

    /**
     * Returns the number of elements in the repository.
     */
    public int size() {
        return storage.size();
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
