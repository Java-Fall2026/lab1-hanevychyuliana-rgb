package ua.hotel;

import ua.hotel.model.Booking;
import ua.hotel.model.Guest;
import ua.hotel.model.HotelPayment;
import ua.hotel.model.Room;
import ua.hotel.util.HotelUtils;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== УСПІШНІ СЦЕНАРІЇ ===");

        // 1. Створення гостя
        Guest guest = new Guest("  AB123456  ", "Іван", "Петренко", LocalDate.of(1995, 5, 20));
        System.out.println("Створено гостя: " + guest);

        // 2. Створення кімнати
        Room room = Room.of(101, "DELUXE", 2500.0);
        System.out.println("Створено кімнату: " + room);

        // Зміна ціни
        room.setPricePerNight(2800.0);
        System.out.println("Оновлено ціну кімнати: " + room.getPricePerNight());

        // 3. Створення бронювання
        Booking booking = Booking.of(
                room,
                guest,
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 15),
                "CONFIRMED"
        );
        System.out.println("Створено бронювання: " + booking);

        // 4. Обчислення
        long nightsCount = HotelUtils.nights(booking);
        double totalCost = HotelUtils.totalPrice(booking);
        System.out.println("Кількість ночей: " + nightsCount);
        System.out.println("Загальна вартість: " + totalCost);
        System.out.println("Резюме: " + HotelUtils.formatBookingSummary(booking));

        // 5. Оплата
        HotelPayment payment = new HotelPayment("PAY-8801", booking, totalCost, LocalDate.of(2026, 10, 10));
        System.out.println("Створено оплату: " + payment);

        System.out.println("\n=== НЕВДАЛІ СЦЕНАРІЇ (ВАЛІДАЦІЯ) ===");

        testException("Неповнолітній гість (<18 років)", () -> 
            new Guest("CD987654", "Олег", "Сидоренко", LocalDate.now().minusYears(16))
        );

        testException("Порожній номер паспорта", () -> 
            new Guest("   ", "Анна", "Коваль", LocalDate.of(1990, 1, 1))
        );

        testException("Некоректний номер кімнати (<= 0)", () -> 
            Room.of(-5, "SINGLE", 1000.0)
        );

        testException("Некоректна дата виїзду (раніше за заїзд)", () -> 
            Booking.of(
                room, guest, 
                LocalDate.of(2026, 10, 15), 
                LocalDate.of(2026, 10, 10), 
                "CONFIRMED"
            )
        );

        testException("Оплата за датою раніше за заїзд", () -> 
            new HotelPayment("PAY-9999", booking, 5000.0, LocalDate.of(2026, 10, 1))
        );
    }

    private static void testException(String scenario, Runnable action) {
        try {
            action.run();
            System.out.println("[FAIL] " + scenario + " — виключення НЕ було викинуто!");
        } catch (IllegalArgumentException e) {
            System.out.println("[SUCCESS] " + scenario + " — перехоплено виключення: " + e.getMessage());
        }
    }
}