package ua.hotel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.hotel.loader.DataLoader;
import ua.hotel.model.Guest;
import ua.hotel.model.Room;
import ua.hotel.model.RoomType;
import ua.hotel.repository.FileStorage;
import ua.hotel.repository.GuestRepository;
import ua.hotel.repository.LoadResult;
import ua.hotel.repository.RoomRepository;
import ua.hotel.util.AppConfig;

import java.nio.file.Path;
import java.util.List;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        System.out.println("=== Лабораторна робота №6: Файли, серіалізація та валідація ===\n");

        try {
            // 1. Reading configuration from app.properties
            Path roomsPath = Path.of(AppConfig.getProperty("rooms.file"));
            Path guestsPath = Path.of(AppConfig.getProperty("guests.file"));
            Path brokenPath = Path.of(AppConfig.getProperty("broken.file"));
            Path savedRoomsPath = Path.of(AppConfig.getProperty("saved.rooms.file"));

            System.out.println("Конфігурацію завантажено:");
            System.out.println("  Шлях до кімнат: " + roomsPath);
            System.out.println("  Шлях до гостей: " + guestsPath);
            System.out.println("  Шлях до битого файлу: " + brokenPath);

            FileStorage<Room> roomStorage = new FileStorage<>(Room.class);
            FileStorage<Guest> guestStorage = new FileStorage<>(Guest.class);

            RoomRepository roomRepository = new RoomRepository();
            GuestRepository guestRepository = new GuestRepository();

            DataLoader<Room> roomDataLoader = new DataLoader<>();
            DataLoader<Guest> guestDataLoader = new DataLoader<>();

            // 2. Loading valid and invalid records from JSON and YAML
            System.out.println("\n--- Завантаження кімнат із JSON ---");
            LoadResult roomResult = roomDataLoader.loadData(roomsPath, roomRepository, roomStorage);
            printLoadResult("Кімнати", roomResult);

            System.out.println("\n--- Завантаження гостей із YAML ---");
            LoadResult guestResult = guestDataLoader.loadData(guestsPath, guestRepository, guestStorage);
            printLoadResult("Гості", guestResult);

            // 3. Attempting to load syntactically broken JSON file
            System.out.println("\n--- Спроба завантаження битого файлу ---");
            LoadResult brokenResult = roomDataLoader.loadData(brokenPath, roomRepository, roomStorage);
            printLoadResult("Битий файл", brokenResult);

            // 4. Modifying repository state and saving to a new file
            System.out.println("\n--- Зміна репозиторію, збереження та повторне завантаження ---");
            roomRepository.removeByIdentity("102");
            roomRepository.add(new Room(999, RoomType.DELUXE, 3500.0));
            List<Room> expectedRooms = roomRepository.findAll();

            roomStorage.save(savedRoomsPath, expectedRooms);
            System.out.println("Збережено " + expectedRooms.size() + " кімнат у файл: " + savedRoomsPath);

            // 5. Reloading saved file and verifying contents match
            RoomRepository reloadedRepo = new RoomRepository();
            LoadResult reloadedResult = roomDataLoader.loadData(savedRoomsPath, reloadedRepo, roomStorage);
            List<Room> actualRooms = reloadedRepo.findAll();

            System.out.println("Повторно завантажено кімнат: " + reloadedResult.addedCount());
            System.out.println("Вміст репозиторіїв повністю збігається: " + expectedRooms.equals(actualRooms));

            log.info("Lab 6 execution completed successfully.");

        } catch (Exception e) {
            log.error("Critical error during application execution: {}", e.getMessage(), e);
            System.err.println("Сталася критична помилка: " + e.getMessage());
        }
    }

    private static void printLoadResult(String entityName, LoadResult result) {
        System.out.println(entityName + " -> Прочитано: " + result.readCount()
                + ", Додано: " + result.addedCount()
                + ", Відкинуто: " + result.rejectedRecords().size());

        if (!result.rejectedRecords().isEmpty()) {
            System.out.println("  Відкинуті записи:");
            for (String rejection : result.rejectedRecords()) {
                System.out.println("    - " + rejection);
            }
        }
    }
}
