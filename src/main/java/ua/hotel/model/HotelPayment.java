package ua.hotel.model;

import ua.common.BaseEntity;
import ua.hotel.util.ValidationHelper;

import java.time.LocalDate;
import java.util.Objects;

public class HotelPayment extends BaseEntity {
    private final String paymentId;
    private final Booking booking;
    private final double amount;
    private final LocalDate paymentDate;

    public HotelPayment(String paymentId, Booking booking, double amount, LocalDate paymentDate) {
        super();
        String trimmedPaymentId = ValidationHelper.requireNonNullOrBlank(paymentId, "paymentId").trim();
        ValidationHelper.requireNonNull(booking, "booking");
        ValidationHelper.requireStrictlyPositive(amount, "amount");
        ValidationHelper.requireNonNull(paymentDate, "paymentDate");

        if (paymentDate.isBefore(booking.getCheckInDate())) {
            throw new IllegalArgumentException(
                "paymentDate cannot be before booking checkInDate. Expected >= " + booking.getCheckInDate() + ", but got: " + paymentDate
            );
        }

        this.paymentId = trimmedPaymentId;
        this.booking = booking;
        this.amount = amount;
        this.paymentDate = paymentDate;
    }

    public String getPaymentId() { return paymentId; }
    public Booking getBooking() { return booking; }
    public double getAmount() { return amount; }
    public LocalDate getPaymentDate() { return paymentDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HotelPayment payment = (HotelPayment) o;
        return Objects.equals(paymentId, payment.paymentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentId);
    }

    @Override
    public String toString() {
        return "HotelPayment{" +
                "paymentId='" + paymentId + '\'' +
                ", booking=" + booking +
                ", amount=" + amount +
                ", paymentDate=" + paymentDate +
                '}';
    }
}