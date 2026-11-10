package ua.hotel;

import ua.hotel.exception.DuplicateIdentityException;
import ua.hotel.loader.RoomCsvLoader;
import ua.hotel.model.Booking;
import ua.hotel.model.BookingStatus;
import ua.hotel.model.Guest;
import ua.hotel.model.HotelPayment;
import ua.hotel.model.Room;
import ua.hotel.model.RoomType;
import ua.hotel.repository.GenericRepository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== Лабораторна робота №4: Generics, колекції та власний репозиторій ===\n");

        // 1. Три репозиторії різних типів (1 змінюваний клас Room + 2 record-и Guest і HotelPayment)
        GenericRepository<Room> roomRepo = new GenericRepository<>(
                "RoomRepository",
                room -> String.valueOf(room.getRoomNumber())
        );

        GenericRepository<Guest> guestRepo = new GenericRepository<>(
                "GuestRepository",
                Guest::passportNumber
        );

        GenericRepository<HotelPayment> paymentRepo = new GenericRepository<>(
                "PaymentRepository",
                HotelPayment::paymentId
        );

        Room room201 = new Room(201, RoomType.SINGLE, 900.0);
        Room room202 = new Room(202, RoomType.DELUXE, 2500.0);
        roomRepo.add(room201);
        roomRepo.add(room202);

        Guest guest1 = new Guest("AA123456", "Юліана", "Ганевич", LocalDate.of(2004, 5, 15), null);
        Guest guest2 = new Guest("BB654321", "Олександр", "Коваль", LocalDate.of(1995, 8, 20), null);
        guestRepo.add(guest1);
        guestRepo.add(guest2);

        Booking booking = new Booking(
                room201,
                guest1,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 5),
                BookingStatus.CONFIRMED
        );
        HotelPayment payment1 = new HotelPayment("PAY-8801", booking, 3600.0, LocalDate.of(2026, 6, 1), null);
        paymentRepo.add(payment1);

        System.out.println("--- 1. Пошук за identity у трьох репозиторіях (знайдено і не знайдено) ---");
        System.out.println("Кімната 201 (знайдено): " + roomRepo.findByIdentity("201"));
        System.out.println("Кімната 999 (не знайдено): " + roomRepo.findByIdentity("999"));
        System.out.println("Гість AA123456 (знайдено): " + guestRepo.findByIdentity("AA123456"));
        System.out.println("Гість XX000000 (не знайдено): " + guestRepo.findByIdentity("XX000000"));
        System.out.println("Оплата PAY-8801 (знайдено): " + paymentRepo.findByIdentity("PAY-8801"));
        System.out.println("Оплата PAY-0000 (не знайдено): " + paymentRepo.findByIdentity("PAY-0000"));

        // 2. Наповнення репозиторію з CSV-файла ЛР3 (містить дублікат кімнати 101)
        System.out.println("\n--- 2. Наповнення репозиторію з CSV-файла (data/rooms.csv) ---");
        GenericRepository<Room> csvRoomRepo = new GenericRepository<>(
                "CsvRoomRepository",
                room -> String.valueOf(room.getRoomNumber())
        );
        RoomCsvLoader loader = new RoomCsvLoader();
        Path csvPath = Paths.get("data", "rooms.csv");
        List<Room> loadedRooms = loader.loadRooms(csvPath);

        for (Room room : loadedRooms) {
            boolean added = csvRoomRepo.addIfAbsent(room);
            if (added) {
                System.out.println("Додано кімнату з CSV: №" + room.getRoomNumber() + " (" + room.getType() + ")");
            } else {
                System.out.println("Відхилено дублікат із CSV: кімната №" + room.getRoomNumber() + " вже є в репозиторії");
            }
        }
        System.out.println("Підсумкова кількість унікальних кімнат у CsvRoomRepository: " + csvRoomRepo.size());

        // 3. Додавання дубліката через add (із перехопленням) і через addIfAbsent
        System.out.println("\n--- 3. Додавання дубліката через add() та addIfAbsent() ---");
        Guest duplicateGuest = new Guest("AA123456", "ІншеІм'я", "ІншеПрізвище", LocalDate.of(1999, 1, 1), null);

        try {
            guestRepo.add(duplicateGuest);
        } catch (DuplicateIdentityException e) {
            System.out.println("Перехоплено DuplicateIdentityException при виклику add(): " + e.getMessage());
        }

        boolean addedIfAbsent = guestRepo.addIfAbsent(duplicateGuest);
        System.out.println("Результат addIfAbsent() на тому ж дублікаті: " + addedIfAbsent
                + " (розмір репозиторію не змінився: " + guestRepo.size() + ")");

        // 4. Видалення наявного й відсутнього елемента
        System.out.println("\n--- 4. Видалення наявного й відсутнього елемента ---");
        boolean removedExisting = roomRepo.removeByIdentity("202");
        boolean removedMissing = roomRepo.removeByIdentity("777");
        System.out.println("Видалення наявної кімнати 202: " + removedExisting);
        System.out.println("Видалення відсутньої кімнати 777: " + removedMissing);
        System.out.println("Поточний розмір RoomRepository: " + roomRepo.size());
    }
}
