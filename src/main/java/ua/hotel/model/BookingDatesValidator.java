package ua.hotel.model;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class BookingDatesValidator implements ConstraintValidator<ValidBookingDates, Booking> {
    @Override
    public boolean isValid(Booking booking, ConstraintValidatorContext context) {
        if (booking == null) return true;
        if (booking.getCheckInDate() == null || booking.getCheckOutDate() == null) return false;
        return booking.getCheckOutDate().isAfter(booking.getCheckInDate());
    }
}