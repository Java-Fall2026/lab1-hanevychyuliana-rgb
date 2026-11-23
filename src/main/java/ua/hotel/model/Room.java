package ua.hotel.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ua.common.BaseEntity;

import java.util.Objects;

public class Room extends BaseEntity implements Comparable<Room> {

    @Positive(message = "Номер кімнати має бути додатним числом")
    private int roomNumber;

    @NotNull(message = "Тип кімнати не може бути порожнім")
    private RoomType type;

    @Positive(message = "Ціна за ніч має бути строго більшою за нуль")
    @Min(value = 10, message = "Мінімальна ціна кімнати становить 10")
    private double pricePerNight;

    // Default constructor required by Jackson deserialization
    public Room() {
        super();
    }

    @JsonCreator
    public Room(
            @JsonProperty("roomNumber") int roomNumber,
            @JsonProperty("type") RoomType type,
            @JsonProperty("pricePerNight") double pricePerNight) {
        super();
        this.roomNumber = roomNumber;
        this.type = type;
        this.pricePerNight = pricePerNight;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public RoomType getType() {
        return type;
    }

    public void setType(RoomType type) {
        this.type = type;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    @Override
    public int compareTo(Room other) {
        if (other == null) {
            return 1;
        }
        int res = Integer.compare(this.roomNumber, other.roomNumber);
        return res != 0 ? res : Double.compare(this.pricePerNight, other.pricePerNight);
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
