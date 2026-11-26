package ua.hotel.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.hotel.exception.ApiException;
import ua.hotel.exception.DuplicateIdentityException;
import ua.hotel.exception.MethodNotAllowedException;
import ua.hotel.exception.NotFoundException;

import java.io.IOException;

public class ErrorHandlingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(ErrorHandlingFilter.class);
    private final JsonSupport json;

    public ErrorHandlingFilter(JsonSupport json) {
        this.json = json;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String path = req.getRequestURI();

        HttpServletResponse wrappedResponse = new HttpServletResponseWrapper(resp) {
            @Override
            public void sendError(int sc) {
                throw mapContainerError(sc, req);
            }

            @Override
            public void sendError(int sc, String msg) {
                throw mapContainerError(sc, req);
            }
        };

        try {
            chain.doFilter(req, wrappedResponse);
        } catch (ApiException e) {
            ApiError error = new ApiError(
                    e.getStatus(),
                    e.getErrorTitle(),
                    e.getUserMessage(),
                    path,
                    e.getViolations()
            );
            json.write(resp, e.getStatus(), error);
        } catch (DuplicateIdentityException e) {
            ApiError error = ApiError.of(
                    409,
                    "Conflict",
                    "Об'єкт із ідентифікатором '" + e.getIdentity() + "' вже існує",
                    path
            );
            json.write(resp, 409, error);
        } catch (IllegalArgumentException e) {
            ApiError error = ApiError.of(
                    400,
                    "Bad Request",
                    "Некоректний запит: " + e.getMessage(),
                    path
            );
            json.write(resp, 400, error);
        } catch (Throwable t) {
            log.error("Unhandled exception while processing {} {}", req.getMethod(), path, t);
            ApiError error = ApiError.of(
                    500,
                    "Internal Server Error",
                    "Внутрішня помилка сервера",
                    path
            );
            json.write(resp, 500, error);
        }
    }

    private ApiException mapContainerError(int statusCode, HttpServletRequest req) {
        if (statusCode == HttpServletResponse.SC_METHOD_NOT_ALLOWED) {
            return new MethodNotAllowedException(
                    "Метод " + req.getMethod() + " не підтримується для шляху " + req.getRequestURI(),
                    "HTTP method not allowed: " + req.getMethod() + " " + req.getRequestURI()
            );
        }
        if (statusCode == HttpServletResponse.SC_NOT_FOUND) {
            return new NotFoundException(
                    "Ресурс за шляхом " + req.getRequestURI() + " не знайдено",
                    "Path not found: " + req.getRequestURI()
            );
        }
        return new ApiException(
                statusCode,
                "HTTP Error " + statusCode,
                "Помилка обробки запиту",
                "Container error status: " + statusCode
        );
    }
}
