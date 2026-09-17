package ua.hotel.model;

import ua.common.BaseEntity;
import ua.hotel.util.ValidationHelper;

import java.time.LocalDate;
import java.util.Objects;

public class Booking extends BaseEntity {
    private final Room room;
    private final Guest guest;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private String status;

    private Booking(Room room, Guest guest, LocalDate checkInDate, LocalDate checkOutDate, String status) {
        super();
        ValidationHelper.requireNonNull(room, "room");
        ValidationHelper.requireNonNull(guest, "guest");
        ValidationHelper.requireNonNull(checkInDate, "checkInDate");
        ValidationHelper.requireNonNull(checkOutDate, "checkOutDate");
        ValidationHelper.requireNonNullOrBlank(status, "status");

        if (!checkOutDate.isAfter(checkInDate)) {
            throw new IllegalArgumentException(
                "checkOutDate must be strictly after checkInDate. Expected > " + checkInDate + ", but got: " + checkOutDate
            );
        }

        this.room = room;
        this.guest = guest;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = status.trim();
    }

    public static Booking of(Room room, Guest guest, LocalDate checkInDate, LocalDate checkOutDate, String status) {
        return new Booking(room, guest, checkInDate, checkOutDate, status);
    }

    public Room getRoom() { return room; }
    public Guest getGuest() { return guest; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public String getStatus() { return status; }

    public void setStatus(String status) {
        ValidationHelper.requireNonNullOrBlank(status, "status");
        this.status = status.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booking booking = (Booking) o;
        return Objects.equals(room, booking.room) &&
                Objects.equals(guest, booking.guest) &&
                Objects.equals(checkInDate, booking.checkInDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(room, guest, checkInDate);
    }

    @Override
    public String toString() {
        return "Booking{" +
                "room=" + room +
                ", guest=" + guest +
                ", checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", status='" + status + '\'' +
                '}';
    }
}