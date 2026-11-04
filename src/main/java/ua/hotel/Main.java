package ua.hotel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.hotel.loader.RoomCsvLoader;
import ua.hotel.model.Room;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        RoomCsvLoader loader = new RoomCsvLoader();

        System.out.println("=== 1. Читання наявного CSV з некоректними рядками ===");
        Path csvPath = Paths.get("data", "rooms.csv");
        List<Room> rooms = loader.loadRooms(csvPath);
        System.out.println("Прочитано коректних кімнат: " + rooms.size());
        rooms.forEach(room -> System.out.println(" - Кімната №" + room.getRoomNumber() + " (" + room.getType() + "): " + room.getPricePerNight() + " UAH"));

        System.out.println("\n=== 2. Спроба прочитати відсутній файл ===");
        Path missingPath = Paths.get("data", "ghost_file.csv");
        List<Room> missingRooms = loader.loadRooms(missingPath);
        System.out.println("Кількість кімнат з відсутнього файла: " + missingRooms.size());

        System.out.println("\n=== 3. Некоректне значення у сеттері змінюваного класу ===");
        if (!rooms.isEmpty()) {
            Room room = rooms.get(0);
            System.out.println("Початкова ціна кімнати #" + room.getRoomNumber() + ": " + room.getPricePerNight());
            try {
                System.out.println("Спроба встановити від'ємну ціну (-500.0 UAH)...");
                room.setPricePerNight(-500.0);
            } catch (IllegalArgumentException e) {
                log.warn("Спроба встановити некоректну ціну: {}", e.getMessage());
                System.out.println("Перехоплено виключення в сеттері: " + e.getMessage());
            }
        }

        System.out.println("\n=== 4. Демонстрація логування трьох рівнів ===");
        log.info("Інформаційне повідомлення (INFO): Програму успішно запущено.");
        log.warn("Попередження (WARN): Демонстрація логування рівню WARN.");
        log.error("Помилка (ERROR): Демонстрація логування рівню ERROR.");
    }
}