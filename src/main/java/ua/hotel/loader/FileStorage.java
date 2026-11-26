package ua.hotel.loader;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Handles reading and writing domain entity collections in JSON format.
 */
public class FileStorage {

    private static final Logger log = LoggerFactory.getLogger(FileStorage.class);
    private final ObjectMapper mapper;

    public FileStorage(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public <T> List<T> readList(Path path, Class<T> elementType) throws IOException {
        if (!Files.exists(path)) {
            log.warn("Input file does not exist: {}", path);
            return List.of();
        }
        JavaType listType = mapper.getTypeFactory().constructCollectionType(List.class, elementType);
        return mapper.readValue(path.toFile(), listType);
    }

    public <T> void writeList(Path path, List<T> items) throws IOException {
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        mapper.writeValue(path.toFile(), items);
        log.info("Saved {} items to {}", items.size(), path);
    }
}
