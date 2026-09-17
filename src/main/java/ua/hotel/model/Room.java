package ua.hotel.model;

import ua.common.BaseEntity;
import ua.hotel.util.ValidationHelper;

import java.util.Objects;

public class Room extends BaseEntity {
    private final int roomNumber;
    private final String roomType;
    private double pricePerNight;

    private Room(int roomNumber, String roomType, double pricePerNight) {
        super();
        ValidationHelper.requireStrictlyPositive(roomNumber, "roomNumber");
        ValidationHelper.requireNonNullOrBlank(roomType, "roomType");
        ValidationHelper.requireStrictlyPositive(pricePerNight, "pricePerNight");

        this.roomNumber = roomNumber;
        this.roomType = roomType.trim();
        this.pricePerNight = pricePerNight;
    }

    public static Room of(int roomNumber, String roomType, double pricePerNight) {
        return new Room(roomNumber, roomType, pricePerNight);
    }

    public int getRoomNumber() { return roomNumber; }
    public String getRoomType() { return roomType; }
    public double getPricePerNight() { return pricePerNight; }

    public void setPricePerNight(double pricePerNight) {
        ValidationHelper.requireStrictlyPositive(pricePerNight, "pricePerNight");
        this.pricePerNight = pricePerNight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return roomNumber == room.roomNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomNumber);
    }

    @Override
    public String toString() {
        return "Room{" +
                "roomNumber=" + roomNumber +
                ", roomType='" + roomType + '\'' +
                ", pricePerNight=" + pricePerNight +
                '}';
    }
}