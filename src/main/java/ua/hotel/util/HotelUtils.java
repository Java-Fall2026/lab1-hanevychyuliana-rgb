package ua.hotel.util;

import ua.hotel.model.Booking;
import ua.hotel.model.BookingStatus;
import ua.hotel.model.RoomType;

import java.time.temporal.ChronoUnit;

/**
 * Utility class containing calculations and switch expressions.
 */
public final class HotelUtils {

    private HotelUtils() {}

    public static long calculateNights(Booking booking) {
        ValidationHelper.requireNonNull(booking, "booking");
        return ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
    }

    public static double calculateTotalPrice(Booking booking) {
        ValidationHelper.requireNonNull(booking, "booking");
        return calculateNights(booking) * booking.getRoom().getPricePerNight();
    }

    // Switch #1: Вичерпний switch по BookingStatus (без default)
    public static String getStatusDescription(BookingStatus status) {
        ValidationHelper.requireNonNull(status, "status");
        return switch (status) {
            case CONFIRMED -> "Бронювання підтверджено, очікується заїзд";
            case CHECKED_IN -> "Гість успішно заселений у номер";
            case CHECKED_OUT -> "Гість виселився, номер звільнено";
            case CANCELLED -> "Бронювання скасовано";
        };
    }

    // Switch #2: Поведінка (коефіцієнт знижки за типом номера)
    public static double getSeasonalDiscountRate(RoomType roomType, boolean isHighSeason) {
        ValidationHelper.requireNonNull(roomType, "roomType");
        return switch (roomType) {
            case SINGLE, DOUBLE -> isHighSeason ? 0.0 : 0.10;
            case SUITE -> isHighSeason ? 0.05 : 0.15;
            case DELUXE -> isHighSeason ? 0.05 : 0.20;
        };
    }
}