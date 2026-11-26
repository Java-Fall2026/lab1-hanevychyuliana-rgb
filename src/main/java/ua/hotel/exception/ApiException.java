package ua.hotel.exception;

import ua.hotel.web.ApiError;

import java.util.List;


public class ApiException extends RuntimeException {

    private final int status;
    private final String errorTitle;
    private final String userMessage;
    private final List<ApiError.Violation> violations;

    public ApiException(int status, String errorTitle, String userMessage, String debugMessage) {
        this(status, errorTitle, userMessage, debugMessage, List.of(), null);
    }

    public ApiException(int status, String errorTitle, String userMessage, String debugMessage, Throwable cause) {
        this(status, errorTitle, userMessage, debugMessage, List.of(), cause);
    }

    public ApiException(
            int status,
            String errorTitle,
            String userMessage,
            String debugMessage,
            List<ApiError.Violation> violations,
            Throwable cause
    ) {
        super(debugMessage, cause);
        this.status = status;
        this.errorTitle = errorTitle;
        this.userMessage = userMessage;
        this.violations = violations == null ? List.of() : List.copyOf(violations);
    }

    public int getStatus() {
        return status;
    }

    public String getErrorTitle() {
        return errorTitle;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public List<ApiError.Violation> getViolations() {
        return violations;
    }
}
