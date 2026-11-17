package ua.hotel.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class AppConfig {

    private static Properties defaultProperties;

    private AppConfig() {
    }

    private static synchronized Properties getDefaultProperties() {
        if (defaultProperties == null) {
            Properties props = new Properties();
            try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream("app.properties")) {
                if (input != null) {
                    props.load(input);
                } else {
                    Path fallbackPath = Path.of("src/main/resources/app.properties");
                    if (Files.exists(fallbackPath)) {
                        try (InputStream fileInput = Files.newInputStream(fallbackPath)) {
                            props.load(fileInput);
                        }
                    } else {
                        throw new IllegalStateException("Configuration file 'app.properties' not found");
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException("Failed to load 'app.properties'", e);
            }
            defaultProperties = props;
        }
        return defaultProperties;
    }

    public static String getProperty(String key) {
        return getProperty(getDefaultProperties(), key);
    }

    public static String getProperty(Properties source, String key) {
        if (source == null) {
            throw new IllegalArgumentException("Properties source must not be null");
        }
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Property key must not be null or blank");
        }
        String value = source.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required configuration key: " + key);
        }
        return value.trim();
    }
}
