package ua.hotel.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ua.common.BaseEntity;
import ua.hotel.util.ValidationHelper;

import java.util.Objects;

public class Room extends BaseEntity implements Comparable<Room> {

    @Positive(message = "Номер кімнати має бути додатним числом")
    private final int roomNumber;

    @NotNull(message = "Тип кімнати не може бути порожнім")
    private final RoomType type;

    @Positive(message = "Ціна за ніч має бути додатною")
    private double pricePerNight;

    @JsonCreator
    public Room(
            @JsonProperty("roomNumber") int roomNumber,
            @JsonProperty("type") RoomType type,
            @JsonProperty("pricePerNight") double pricePerNight
    ) {
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
        int result = Integer.compare(this.roomNumber, other.roomNumber);
        if (result == 0) {
            result = Double.compare(this.pricePerNight, other.pricePerNight);
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Room room)) return false;
        return roomNumber == room.roomNumber
                && Double.compare(pricePerNight, room.pricePerNight) == 0
                && type == room.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomNumber, type, pricePerNight);
    }
}
