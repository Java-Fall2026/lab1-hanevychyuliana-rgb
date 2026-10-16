package ua.hotel.model;

import ua.common.BaseEntity;
import ua.hotel.util.ValidationHelper;
import java.time.LocalDate;


public class Booking extends BaseEntity {
    private final Room room;
    private final Guest guest;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private BookingStatus status;

    public Booking(Room room, Guest guest, LocalDate checkInDate, LocalDate checkOutDate, BookingStatus status) {
        super();
        ValidationHelper.requireNonNull(room, "room");
        ValidationHelper.requireNonNull(guest, "guest");
        ValidationHelper.requireNonNull(checkInDate, "checkInDate");
        ValidationHelper.requireNonNull(checkOutDate, "checkOutDate");
        ValidationHelper.requireNonNull(status, "status");

        if (!checkOutDate.isAfter(checkInDate)) {
            throw new IllegalArgumentException("checkOutDate must be strictly after checkInDate");
        }

        this.room = room;
        this.guest = guest;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = status;
    }

    public Room getRoom() { return room; }
    public Guest getGuest() { return guest; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public BookingStatus getStatus() { return status; }

    public void setStatus(BookingStatus status) {
        ValidationHelper.requireNonNull(status, "status");
        this.status = status;
    }
}