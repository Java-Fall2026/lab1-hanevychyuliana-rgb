package ua.hotel.util;

public class ValidationHelper {

    public static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException("Field '" + fieldName + "' cannot be null. Expected non-null value, but got: null");
        }
        return value;
    }

    public static String requireNonNullOrBlank(String value, String fieldName) {
        requireNonNull(value, fieldName);
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("Field '" + fieldName + "' cannot be empty/blank. Got: '" + value + "'");
        }
        return value;
    }

    public static int requireStrictlyPositive(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException("Field '" + fieldName + "' must be strictly positive (> 0). Expected > 0, but got: " + value);
        }
        return value;
    }

    public static double requireStrictlyPositive(double value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException("Field '" + fieldName + "' must be strictly positive (> 0). Expected > 0, but got: " + value);
        }
        return value;
    }
}