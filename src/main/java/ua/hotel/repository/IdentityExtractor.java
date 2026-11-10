package ua.hotel.repository;

/**
 * Functional interface for extracting a unique string identity from an entity.
 *
 * @param <T> the type of the entity
 */
@FunctionalInterface
public interface IdentityExtractor<T> {

    /**
     * Extracts the unique identity of the given item.
     *
     * @param item the entity instance
     * @return unique string identifier
     */
    String identityOf(T item);
}
