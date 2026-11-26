package ua.hotel.web;

import java.util.List;


public record ApiError(
        int status,
        String error,
        String message,
        String path,
        List<Violation> violations
) {
    public record Violation(String field, String message) {
    }

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(status, error, message, path, List.of());
    }

    public static ApiError validation(String message, String path, List<Violation> violations) {
        return new ApiError(400, "Bad Request", message, path, List.copyOf(violations));
    }
}
