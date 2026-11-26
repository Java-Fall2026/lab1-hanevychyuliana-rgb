package ua.hotel.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

public class AppConfig {

    private final Properties properties;

    public AppConfig(Properties properties) {
        this.properties = properties;
    }

    public static AppConfig loadFromClasspath(String resourceName) {
        Properties props = new Properties();
        try (InputStream is = AppConfig.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new IllegalStateException("Configuration file not found on classpath: " + resourceName);
            }
            props.load(is);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load configuration file: " + resourceName, e);
        }
        return new AppConfig(props);
    }

    public int getPort() {
        String raw = getRequiredProperty("server.port");
        try {
            int port = Integer.parseInt(raw.trim());
            if (port < 0 || port > 65535) {
                throw new IllegalArgumentException("Port out of valid range (0..65535): " + port);
            }
            return port;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer value for server.port: " + raw, e);
        }
    }

    public Path getPath(String key) {
        return Path.of(getRequiredProperty(key).trim());
    }

    private String getRequiredProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required configuration key: " + key);
        }
        return value;
    }
}
