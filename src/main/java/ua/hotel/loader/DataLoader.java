package ua.hotel.loader;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import ua.hotel.repository.LoadResult;
import ua.hotel.repository.GenericRepository;
import ua.hotel.repository.FileStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DataLoader<T> {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);
    private final Validator validator;

    public DataLoader() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            this.validator = factory.getValidator();
        }
    }

    public LoadResult loadData(Path file, GenericRepository<T> repository, FileStorage<T> storage) {
        List<String> rejected = new ArrayList<>();
        int readCount = 0;
        int addedCount = 0;

        if (file == null || !Files.exists(file)) {
            log.error("File not found or path is null: {}", file);
            return new LoadResult(0, 0, List.of("File not found: " + file));
        }

        List<T> items;
        try {
            items = storage.load(file);
        } catch (IOException | RuntimeException e) {
            log.error("Failed to read or parse file due to syntax/IO error: {}", file, e);
            return new LoadResult(0, 0, List.of("Failed to read file (syntax or IO error): " + e.getMessage()));
        }

        if (items == null) {
            return new LoadResult(0, 0, List.of("File content evaluated to null"));
        }

        readCount = items.size();
        int index = 0;

        for (T item : items) {
            index++;
            if (item == null) {
                rejected.add("Record #" + index + ": item is null");
                log.warn("Record #{} in file {} is null", index, file);
                continue;
            }

            Set<ConstraintViolation<T>> violations = validator.validate(item);
            if (!violations.isEmpty()) {
                StringBuilder sb = new StringBuilder("Record #" + index + " validation failed: ");
                for (ConstraintViolation<T> v : violations) {
                    sb.append(String.format("[Field: '%s', Error: '%s'] ", v.getPropertyPath(), v.getMessage()));
                }
                String errorMsg = sb.toString();
                rejected.add(errorMsg);
                log.warn("{}", errorMsg);
                continue;
            }

            try {
                boolean added = repository.addIfAbsent(item);
                if (added) {
                    addedCount++;
                } else {
                    String dupMsg = "Record #" + index + " rejected: duplicate identity in repository";
                    rejected.add(dupMsg);
                    log.warn("{}", dupMsg);
                }
            } catch (Exception e) {
                String err = "Record #" + index + " rejected due to error: " + e.getMessage();
                rejected.add(err);
                log.warn("{}", err);
            }
        }

        log.info("Finished processing file '{}'. Read: {}, Added: {}, Rejected: {}", file, readCount, addedCount, rejected.size());
        return new LoadResult(readCount, addedCount, rejected);
    }
}
