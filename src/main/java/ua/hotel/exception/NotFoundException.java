package ua.hotel.exception;

public class NotFoundException extends ApiException {

    public NotFoundException(String userMessage, String debugMessage) {
        super(404, "Not Found", userMessage, debugMessage);
    }
}
