package ua.hotel.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.hotel.exception.InvalidDataException;
import ua.hotel.model.Room;
import ua.hotel.model.RoomType;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RoomCsvLoader {
    private static final Logger log = LoggerFactory.getLogger(RoomCsvLoader.class);

    public List<Room> loadRooms(Path path) {
        List<Room> rooms = new ArrayList<>();
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
            String line = reader.readLine(); // Skip CSV header
            lineNumber++;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    Room room = parseLine(line, lineNumber);
                    rooms.add(room);
                } catch (InvalidDataException e) {
                    log.warn("Skipping line {}: {}", lineNumber, e.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            log.error("File not found at path: {}", path, e);
        } catch (IOException e) {
            log.error("I/O error reading file at path: {}", path, e);
        } finally {
            log.info("Finished processing file execution flow for: {}", path);
        }

        log.info("Successfully loaded {} valid rooms from {}", rooms.size(), path.getFileName());
        return rooms;
    }

    private Room parseLine(String line, int lineNumber) throws InvalidDataException {
        String[] parts = line.split(",");
        if (parts.length != 3) {
            throw new InvalidDataException("Invalid column count at line " + lineNumber + ". Expected 3, got: " + parts.length);
        }

        try {
            int roomNumber = Integer.parseInt(parts[0].trim());
            RoomType roomType = RoomType.valueOf(parts[1].trim().toUpperCase());
            double price = Double.parseDouble(parts[2].trim());

            return new Room(roomNumber, roomType, price);
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException("Corrupted data at line " + lineNumber + ": " + e.getMessage(), e);
        }
    }
}