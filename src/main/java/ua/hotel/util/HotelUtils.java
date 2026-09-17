package ua.hotel.util;

import ua.hotel.model.Booking;

import java.time.temporal.ChronoUnit;

public class HotelUtils {

    private HotelUtils() {
    }

    public static long nights(Booking booking) {
        ValidationHelper.requireNonNull(booking, "booking");
        return ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
    }

    public static double totalPrice(Booking booking) {
        ValidationHelper.requireNonNull(booking, "booking");
        return nights(booking) * booking.getRoom().getPricePerNight();
    }

    public static String formatBookingSummary(Booking booking) {
        ValidationHelper.requireNonNull(booking, "booking");
        return String.format(
            "Booking [%s - %s]: Room #%d (%s) for guest %s %s. Total (%d nights): %s",
            FormatHelper.formatDate(booking.getCheckInDate()),
            FormatHelper.formatDate(booking.getCheckOutDate()),
            booking.getRoom().getRoomNumber(),
            booking.getRoom().getRoomType(),
            booking.getGuest().getFirstName(),
            booking.getGuest().getLastName(),
            nights(booking),
            FormatHelper.formatCurrency(totalPrice(booking))
        );
    }
}