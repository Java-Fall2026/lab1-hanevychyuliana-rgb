package ua.hotel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.hotel.model.Guest;
import ua.hotel.model.Room;
import ua.hotel.model.RoomType;
import ua.hotel.repository.GuestRepository;
import ua.hotel.repository.RoomRepository;
import ua.hotel.repository.SortOrder;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        System.out.println("=== Лабораторна робота №5: Comparable, Comparator та Stream API ===\n");

        // 1. Наповнення двох доменних репозиторіїв
        RoomRepository roomRepo = new RoomRepository();
        GuestRepository guestRepo = new GuestRepository();

        roomRepo.add(new Room(101, RoomType.SINGLE, 900.0));
        roomRepo.add(new Room(102, RoomType.DOUBLE, 1500.0));
        roomRepo.add(new Room(103, RoomType.SUITE, 2800.0));
        roomRepo.add(new Room(104, RoomType.SINGLE, 950.0)); // Схожа ціна для тесту

        guestRepo.add(new Guest("AA123456", "Юліана", "Ганевич", LocalDate.of(2004, 5, 15), null));
        guestRepo.add(new Guest("BB654321", "Олександр", "Коваль", LocalDate.of(1995, 8, 20), null));
        guestRepo.add(new Guest("CC111222", "Марія", "Петренко", LocalDate.of(1990, 2, 10), null));

        // 2. Сортування за природним порядком та доменними критеріями (багаторівневий)
        System.out.println("--- 2. Сортування кімнат ---");
        List<Room> sortedByPriceAndNum = roomRepo.findAllSortedByPriceThenNumber();
        System.out.println("Кімнати за ціною (зрост.) потім за номером: " + sortedByPriceAndNum.size() + " шт.");

        List<Room> sortedByTypeAndPrice = roomRepo.findAllSortedByTypeAndPriceDesc();
        System.out.println("Кімнати за типом потім за ціною (спад.): " + sortedByTypeAndPrice.size() + " шт.");

        // 3. findAllSortedByIdentity в обидва боки (ASC і DESC)
        System.out.println("\n--- 3. findAllSortedByIdentity (ASC та DESC) ---");
        List<Guest> guestsAsc = guestRepo.findAllSortedByIdentity(SortOrder.ASC);
        List<Guest> guestsDesc = guestRepo.findAllSortedByIdentity(SortOrder.DESC);
        System.out.println("Гості ASC (перший паспорт): " + (guestsAsc.isEmpty() ? "N/A" : guestsAsc.get(0).passportNumber()));
        System.out.println("Гості DESC (перший паспорт): " + (guestsDesc.isEmpty() ? "N/A" : guestsDesc.get(0).passportNumber()));

        // 4. TreeSet із компаратором, не узгодженим з equals (Частина 1, пункт 5)
        System.out.println("\n--- 4. TreeSet із компаратором за ціною ---");
        // Компаратор порівнює кімнати ТІЛЬКИ за ціною. Якщо ціни однакові, вважає їх рівними (0),
        // навіть якщо номери кімнат різні!
        Comparator<Room> priceOnlyComparator = Comparator.comparingDouble(Room::getPricePerNight);
        Set<Room> treeSet = new TreeSet<>(priceOnlyComparator);

        Room r1 = new Room(101, RoomType.SINGLE, 1000.0);
        Room r2 = new Room(104, RoomType.SINGLE, 1000.0); // Інший номер, але така сама ціна!

        treeSet.add(r1);
        treeSet.add(r2);
        System.out.println("Кількість елементів у TreeSet (при однаковій ціні та не узгодженому equals): " + treeSet.size());
        System.out.println("(Очікуємо 1, бо TreeSet вважає їх дублікатами через compareTo/compare, який повертає 0)");

        // 5. Пошук через Stream API (точний/частковий, діапазон, дві умови, Optional)
        System.out.println("\n--- 5. Пошук через Stream API ---");
        System.out.println("Пошук кімнат із типом 'SINGLE': " + roomRepo.searchByType("SINGLE").size() + " шт.");
        System.out.println("Пошук кімнат у діапазоні цін [900, 1600]: " + roomRepo.searchByPriceRange(900.0, 1600.0).size() + " шт.");
        
        // Робота з Optional (без get() + isPresent, а через orElse, ifPresent тощо за умовою)
        roomRepo.findFirstCheapestRoom().ifPresent(room -> 
            System.out.println("Знайдено найдешевшу кімнату: №" + room.getRoomNumber() + " за " + room.getPricePerNight() + " UAH")
        );

        // 6. Групування та агрегація
        System.out.println("\n--- 6. Групування та агрегація ---");
        System.out.println("Середня ціна кімнат: " + roomRepo.getAveragePrice() + " UAH");
        System.out.println("Групування гостей за роком народження (counting): " + guestRepo.countGuestsByBirthYear());

        // 7. Одноразовість стріму (Частина 2, пункт 10)
        System.out.println("\n--- 7. Демонстрація одноразовості стріму ---");
        Stream<Room> roomStream = roomRepo.stream();
        long count = roomStream.count(); // Перше споживання стріму
        System.out.println("Успішно пораховано елементів у стрімі: " + count);

        try {
            // Друга спроба використати той самий стрім має кинути IllegalStateException
            roomStream.forEach(System.out::println);
        } catch (IllegalStateException e) {
            log.warn("Перехоплено виключення повторного використання стріму: {}", e.getMessage());
            System.out.println("Перехоплено очікуване виключення: стрім уже закритий або спожитий!");
        }

        log.info("Програму ЛР5 успішно виконано.");
    }
}
