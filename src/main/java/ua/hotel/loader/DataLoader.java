package ua.hotel.loader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.hotel.exception.DuplicateIdentityException;
import ua.hotel.repository.GenericRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class DataLoader {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final ObjectMapper mapper;
    private final Validator validator;

    public record LoadReport(int read, int added, int rejected) {
    }

    public DataLoader(ObjectMapper mapper, Validator validator) {
        this.mapper = mapper;
        this.validator = validator;
    }

    public <T> LoadReport load(Path path, Class<T> type, GenericRepository<T> repository) {
        int read = 0;
        int added = 0;
        int rejected = 0;

        if (!Files.exists(path)) {
            log.warn("Data file not found: {}", path);
            return new LoadReport(0, 0, 0);
        }

        try {
            JsonNode root = mapper.readTree(path.toFile());
            if (!root.isArray()) {
                log.error("Expected JSON array in file: {}", path);
                return new LoadReport(0, 0, 0);
            }

            for (JsonNode node : root) {
                read++;
                try {
                    T item = mapper.treeToValue(node, type);
                    Set<ConstraintViolation<T>> violations = validator.validate(item);
                    if (!violations.isEmpty()) {
                        rejected++;
                        log.warn("Rejected invalid record #{} from {}: {}", read, path.getFileName(), violations);
                        continue;
                    }
                    repository.add(item);
                    added++;
                } catch (DuplicateIdentityException e) {
                    rejected++;
                    log.warn("Rejected duplicate record #{} from {}: {}", read, path.getFileName(), e.getMessage());
                } catch (Exception e) {
                    rejected++;
                    log.warn("Rejected malformed record #{} from {}: {}", read, path.getFileName(), e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("Failed to read file {}: {}", path, e.getMessage(), e);
        }

        return new LoadReport(read, added, rejected);
    }
}
