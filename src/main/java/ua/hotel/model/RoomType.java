package ua.hotel.model;


public enum RoomType {
    SINGLE(1.0),
    DOUBLE(1.6),
    SUITE(2.5),
    DELUXE(3.2);

    private final double priceFactor;

    RoomType(double priceFactor) {
        this.priceFactor = priceFactor;
    }

    public double getPriceFactor() {
        return priceFactor;
    }
}