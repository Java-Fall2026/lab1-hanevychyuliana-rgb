package ua.hotel;

import ua.hotel.model.*;
import ua.hotel.util.HotelUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== 1. Створення Record та доступ до компонентів ===");
        Guest guest1 = new Guest(" AA123456 ", "Іван", "Петренко", LocalDate.of(1995, 5, 20), LocalDateTime.now());
        System.out.println("Паспорт (пробіли обрізано): " + guest1.passportNumber());
        System.out.println("Ім'я та прізвище: " + guest1.firstName() + " " + guest1.lastName());

        System.out.println("\n=== 2. Перебір values(), доступ до поля enum та valueOf у try/catch ===");
        System.out.println("Константи RoomType та їх цінові коефіцієнти:");
        for (RoomType type : RoomType.values()) {
            System.out.println(" - " + type.name() + ": factor = " + type.getPriceFactor());
        }

        try {
            System.out.println("Спроба зчитати некоректний статус через valueOf():");
            BookingStatus.valueOf("WRONG_STATUS");
        } catch (IllegalArgumentException e) {
            System.out.println("Перехоплено виняток: " + e.getMessage());
        }

        System.out.println("\n=== 3. Обидва switch-вирази в роботі ===");
        BookingStatus status = BookingStatus.CONFIRMED;
        System.out.println("Опис статусу (Switch #1): " + HotelUtils.getStatusDescription(status));

        RoomType roomType = RoomType.DELUXE;
        double discount = HotelUtils.getSeasonalDiscountRate(roomType, false);
        System.out.println("Знижка для DELUXE у низький сезон (Switch #2): " + (discount * 100) + "%");

        System.out.println("\n=== 4. Record з невалідними даними (IllegalArgumentException) ===");
        try {
            System.out.println("Спроба створити гостя, молодшого за 18 років:");
            new Guest("BB999999", "Марія", "Коваль", LocalDate.now().minusYears(15), LocalDateTime.now());
        } catch (IllegalArgumentException e) {
            System.out.println("Успішно перехоплено виняток з компактного конструктора: " + e.getMessage());
        }

        System.out.println("\n=== 5. Однакові record-об'єкти (equals та hashCode) ===");
        Guest guest2 = new Guest("AA123456", "Іван", "Петренко", LocalDate.of(1995, 5, 20), guest1.createdAt());
        System.out.println("guest1.equals(guest2): " + guest1.equals(guest2));
        System.out.println("guest1.hashCode() == guest2.hashCode(): " + (guest1.hashCode() == guest2.hashCode()));

        System.out.println("\n=== 6. Вивід record через згенерований toString() ===");
        System.out.println("Згенерований toString(): " + guest1);
    }
}