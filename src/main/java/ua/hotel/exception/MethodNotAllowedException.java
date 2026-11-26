package ua.hotel.exception;

public class MethodNotAllowedException extends ApiException {

    public MethodNotAllowedException(String userMessage, String debugMessage) {
        super(405, "Method Not Allowed", userMessage, debugMessage);
    }
}
