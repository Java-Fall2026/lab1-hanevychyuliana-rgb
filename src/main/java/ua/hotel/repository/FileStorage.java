package ua.hotel.repository;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class FileStorage<T> {

    private final Class<T> type;
    private final ObjectMapper jsonMapper;
    private final ObjectMapper yamlMapper;

    public FileStorage(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type class must not be null");
        }
        this.type = type;

        this.jsonMapper = new ObjectMapper();
        this.jsonMapper.registerModule(new JavaTimeModule());
        this.jsonMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        this.yamlMapper = new YAMLMapper();
        this.yamlMapper.registerModule(new JavaTimeModule());
        this.yamlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public DataFormat resolveFormat(Path file) {
        if (file == null || file.getFileName() == null) {
            throw new IllegalArgumentException("File path must not be null");
        }
        String fileName = file.getFileName().toString().toLowerCase();
        if (fileName.endsWith(".json")) {
            return DataFormat.JSON;
        } else if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
            return DataFormat.YAML;
        } else {
            throw new IllegalArgumentException("Unknown file extension for file: " + file);
        }
    }

    public List<T> load(Path file) throws IOException {
        DataFormat format = resolveFormat(file);
        ObjectMapper mapper = (format == DataFormat.JSON) ? jsonMapper : yamlMapper;
        JavaType javaType = mapper.getTypeFactory().constructCollectionType(List.class, type);
        return mapper.readValue(file.toFile(), javaType);
    }

    public void save(Path file, List<T> items) throws IOException {
        DataFormat format = resolveFormat(file);
        ObjectMapper mapper = (format == DataFormat.JSON) ? jsonMapper : yamlMapper;
        mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), items);
    }
}
