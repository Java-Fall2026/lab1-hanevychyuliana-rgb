package ua.hotel.exception;

public class UnsupportedMediaTypeException extends ApiException {

    public UnsupportedMediaTypeException(String userMessage, String debugMessage) {
        super(415, "Unsupported Media Type", userMessage, debugMessage);
    }
}
