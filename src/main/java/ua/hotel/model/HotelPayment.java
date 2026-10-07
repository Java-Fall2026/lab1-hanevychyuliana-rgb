package ua.hotel.model;

import ua.hotel.util.ValidationHelper;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record HotelPayment(
        String paymentId,
        Booking booking,
        double amount,
        LocalDate paymentDate,
        LocalDateTime createdAt
) {
    public HotelPayment {
        paymentId = ValidationHelper.requireNonEmpty(paymentId, "paymentId");
        ValidationHelper.requireNonNull(booking, "booking");
        ValidationHelper.requirePositive(amount, "amount");
        ValidationHelper.requireNonNull(paymentDate, "paymentDate");

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (paymentDate.isBefore(booking.getCheckInDate())) {
            throw new IllegalArgumentException("paymentDate cannot be earlier than checkInDate");
        }
    }
}