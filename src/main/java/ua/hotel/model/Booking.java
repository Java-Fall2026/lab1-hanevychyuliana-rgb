package ua.hotel.model;

import jakarta.validation.constraints.NotNull;
import ua.common.BaseEntity;

import java.time.LocalDate;

@ValidBookingDates(message = "Дата виїзду має бути строго пізнішою за дату заїзду")
public class Booking extends BaseEntity {

    @NotNull(message = "Кімната не може бути null")
    private final Room room;

    @NotNull(message = "Гість не може бути null")
    private final Guest guest;

    @NotNull(message = "Дата заїзду не може бути null")
    private final LocalDate checkInDate;

    @NotNull(message = "Дата виїзду не може бути null")
    private final LocalDate checkOutDate;

    @NotNull(message = "Статус бронювання не може бути null")
    private BookingStatus status;

    public Booking(Room room, Guest guest, LocalDate checkInDate, LocalDate checkOutDate, BookingStatus status) {
        super();
        this.room = room;
        this.guest = guest;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = status;
    }

    public Room getRoom() {
        return room;
    }

    public Guest getGuest() {
        return guest;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}
