package ua.hotel.util;

public final class ValidationHelper {
    private ValidationHelper() {}

    public static <T> T requireNonNull(T obj, String paramName) {
        if (obj == null) {
            throw new IllegalArgumentException(paramName + " cannot be null");
        }
        return obj;
    }

    public static String requireNonEmpty(String str, String paramName) {
        requireNonNull(str, paramName);
        if (str.isBlank()) {
            throw new IllegalArgumentException(paramName + " cannot be empty");
        }
        return str;
    }

    public static String requireNonEmptyTrimmed(String str, String paramName) {
        return requireNonEmpty(str, paramName).trim();
    }

    public static double requirePositive(double value, String paramName) {
        if (value <= 0) {
            throw new IllegalArgumentException(paramName + " must be strictly positive");
        }
        return value;
    }

    public static int requirePositive(int value, String paramName) {
        if (value <= 0) {
            throw new IllegalArgumentException(paramName + " must be strictly positive");
        }
        return value;
    }
}