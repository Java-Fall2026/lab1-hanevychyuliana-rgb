package ua.hotel.model;

import ua.common.BaseEntity;
import ua.hotel.util.ValidationHelper;

/**
 * Represents a hotel room.
 */
public class Room extends BaseEntity {
    private final int roomNumber;
    private final RoomType type;
    private double pricePerNight;

    public Room(int roomNumber, RoomType type, double pricePerNight) {
        super();
        ValidationHelper.requirePositive(roomNumber, "roomNumber");
        ValidationHelper.requireNonNull(type, "type");
        setPricePerNight(pricePerNight);
        this.roomNumber = roomNumber;
        this.type = type;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public RoomType getType() {
        return type;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        ValidationHelper.requirePositive(pricePerNight, "pricePerNight");
        this.pricePerNight = pricePerNight;
    }
}