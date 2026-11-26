package ua.hotel.web;

import jakarta.servlet.http.HttpServletRequest;
import ua.hotel.exception.BadRequestException;
import ua.hotel.exception.NotFoundException;
import ua.hotel.repository.SortOrder;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RequestHelper {

    private RequestHelper() {
    }

    public sealed interface RouteTarget permits RouteTarget.Collection, RouteTarget.Stats, RouteTarget.Item {
        record Collection() implements RouteTarget {}
        record Stats() implements RouteTarget {}
        record Item(String id) implements RouteTarget {}
    }

    public static RouteTarget parseRoute(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty() || "/".equals(pathInfo)) {
            return new RouteTarget.Collection();
        }

        String trimmed = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        if (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }

        if (trimmed.isEmpty()) {
            return new RouteTarget.Collection();
        }

        if (trimmed.contains("/")) {
            throw new NotFoundException(
                    "Запитаний шлях не знайдено",
                    "Nested path segments are not supported: " + pathInfo
            );
        }

        String decoded = URLDecoder.decode(trimmed, StandardCharsets.UTF_8);
        if ("stats".equals(decoded)) {
            return new RouteTarget.Stats();
        }
        return new RouteTarget.Item(decoded);
    }

    public static void validateAllowedParams(HttpServletRequest request, Set<String> allowedParams) {
        for (String paramName : request.getParameterMap().keySet()) {
            if (!allowedParams.contains(paramName)) {
                throw new BadRequestException(
                        "Невідомий параметр запиту: " + paramName,
                        "Unknown query parameter: " + paramName
                );
            }
        }
    }

    public static <T> Comparator<T> resolveComparator(
            HttpServletRequest request,
            Map<String, Comparator<T>> comparators
    ) {
        String sortParam = request.getParameter("sort");
        String orderParam = request.getParameter("order");

        SortOrder order = SortOrder.ASC;
        if (orderParam != null && !orderParam.isBlank()) {
            try {
                order = SortOrder.valueOf(orderParam.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(
                        "Некоректне значення параметра order: " + orderParam + ". Дозволено: ASC або DESC",
                        "Invalid order parameter: " + orderParam
                );
            }
        }

        if (sortParam == null || sortParam.isBlank()) {
            if (orderParam != null && !orderParam.isBlank()) {
                throw new BadRequestException(
                        "Параметр order не можна використовувати без параметра sort",
                        "Parameter 'order' provided without 'sort'"
                );
            }
            return null;
        }

        Comparator<T> comparator = comparators.get(sortParam.trim());
        if (comparator == null) {
            throw new BadRequestException(
                    "Невідоме поле сортування sort: " + sortParam,
                    "Unknown sort field: " + sortParam
            );
        }

        return order == SortOrder.DESC ? comparator.reversed() : comparator;
    }

    public static <T> List<T> paginate(HttpServletRequest request, List<T> items) {
        int offset = parseNonNegativeInt(request.getParameter("offset"), "offset", 0);
        int limit = parsePositiveInt(request.getParameter("limit"), "limit", Integer.MAX_VALUE);

        if (offset >= items.size()) {
            return List.of();
        }
        int toIndex = (int) Math.min((long) offset + limit, items.size());
        return items.subList(offset, toIndex);
    }

    public static Double parseDoubleParam(String raw, String name) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
            throw new BadRequestException(
                    "Параметр " + name + " має бути числом",
                    "Invalid numeric parameter '" + name + "': " + raw
            );
        }
    }

    public static Integer parseIntParam(String raw, String name) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new BadRequestException(
                    "Параметр " + name + " має бути цілим числом",
                    "Invalid integer parameter '" + name + "': " + raw
            );
        }
    }

    private static int parseNonNegativeInt(String raw, String name, int defaultValue) {
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            int val = Integer.parseInt(raw.trim());
            if (val < 0) {
                throw new BadRequestException(
                        "Параметр " + name + " не може бути від'ємним",
                        "Negative parameter '" + name + "': " + val
                );
            }
            return val;
        } catch (NumberFormatException e) {
            throw new BadRequestException(
                    "Параметр " + name + " має бути цілим невід'ємним числом",
                    "Invalid parameter '" + name + "': " + raw
            );
        }
    }

    private static int parsePositiveInt(String raw, String name, int defaultValue) {
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            int val = Integer.parseInt(raw.trim());
            if (val <= 0) {
                throw new BadRequestException(
                        "Параметр " + name + " має бути додатним числом (> 0)",
                        "Non-positive parameter '" + name + "': " + val
                );
            }
            return val;
        } catch (NumberFormatException e) {
            throw new BadRequestException(
                    "Параметр " + name + " має бути додатним цілим числом",
                    "Invalid parameter '" + name + "': " + raw
            );
        }
    }
}
