package ua.hotel.exception;

import ua.hotel.web.ApiError;

import java.util.List;


public class BadRequestException extends ApiException {

    public BadRequestException(String userMessage, String debugMessage) {
        super(400, "Bad Request", userMessage, debugMessage);
    }

    public BadRequestException(String userMessage, String debugMessage, Throwable cause) {
        super(400, "Bad Request", userMessage, debugMessage, cause);
    }

    public BadRequestException(String userMessage, List<ApiError.Violation> violations) {
        super(400, "Bad Request", userMessage, "Request body validation failed", violations, null);
    }
}
