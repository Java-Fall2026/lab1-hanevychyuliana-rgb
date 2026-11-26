package ua.hotel.web;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import ua.hotel.exception.BadRequestException;
import ua.hotel.exception.UnsupportedMediaTypeException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class JsonSupport {

    private static final String JSON_MEDIA_TYPE = "application/json";
    private static final String JSON_CONTENT_TYPE_UTF8 = "application/json; charset=UTF-8";

    private final ObjectMapper mapper;
    private final Validator validator;

    public JsonSupport(ObjectMapper mapper, Validator validator) {
        this.mapper = mapper;
        this.validator = validator;
    }

    public <T> T readValid(HttpServletRequest request, Class<T> type) throws IOException {
        String contentType = request.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith(JSON_MEDIA_TYPE)) {
            throw new UnsupportedMediaTypeException(
                    "Очікується заголовок Content-Type: application/json",
                    "Invalid Content-Type: " + contentType
            );
        }

        final T entity;
        try {
            entity = mapper.readValue(request.getInputStream(), type);
        } catch (ValueInstantiationException e) {
            Throwable cause = e.getCause();
            String detail = cause != null ? cause.getMessage() : e.getOriginalMessage();
            throw new BadRequestException(
                    "Некоректні значення полів у тілі запиту: " + detail,
                    "Constructor validation failed during JSON deserialization",
                    e
            );
        } catch (JacksonException e) {
            throw new BadRequestException(
                    "Некоректний формат JSON у тілі запиту",
                    "Malformed JSON payload: " + e.getOriginalMessage(),
                    e
            );
        }

        if (entity == null) {
            throw new BadRequestException(
                    "Тіло запиту не може бути порожнім",
                    "Request body deserialized to null"
            );
        }

        Set<ConstraintViolation<T>> violations = validator.validate(entity);
        if (!violations.isEmpty()) {
            List<ApiError.Violation> errorViolations = violations.stream()
                    .map(v -> new ApiError.Violation(v.getPropertyPath().toString(), v.getMessage()))
                    .sorted(Comparator.comparing(ApiError.Violation::field))
                    .toList();
            throw new BadRequestException("Тіло запиту не пройшло валідацію", errorViolations);
        }

        return entity;
    }

    public <T> void write(HttpServletResponse response, int status, T body) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(JSON_CONTENT_TYPE_UTF8);
        mapper.writeValue(response.getOutputStream(), body);
    }
}
