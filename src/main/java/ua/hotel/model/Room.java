package ua.hotel.model;

import ua.common.BaseEntity;
import ua.hotel.util.ValidationHelper;
public class Room extends BaseEntity implements Comparable<Room> {
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

    @Override
    public int compareTo(Room other) {
        if (other == null) {
            return 1;
        }
        // Natural ordering by roomNumber (primary) and pricePerNight (secondary)
        int result = Integer.compare(this.roomNumber, other.roomNumber);
        if (result == 0) {
            result = Double.compare(this.pricePerNight, other.pricePerNight);
        }
        return result;
    }
}
