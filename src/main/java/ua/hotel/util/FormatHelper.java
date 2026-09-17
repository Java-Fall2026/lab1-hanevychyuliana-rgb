package ua.hotel.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FormatHelper {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static String formatDate(LocalDate date) {
        if (date == null) return "N/A";
        return date.format(DATE_FORMATTER);
    }

    public static String formatCurrency(double amount) {
        return String.format("%.2f UAH", amount);
    }
}