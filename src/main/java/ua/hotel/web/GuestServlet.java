package ua.hotel.web;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ua.hotel.exception.BadRequestException;
import ua.hotel.exception.MethodNotAllowedException;
import ua.hotel.exception.NotFoundException;
import ua.hotel.model.Guest;
import ua.hotel.repository.GuestRepository;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GuestServlet extends HttpServlet {

    private static final Set<String> ALLOWED_QUERY_PARAMS = Set.of(
            "lastName", "minBirthYear", "maxBirthYear", "sort", "order", "limit", "offset"
    );

    private static final Map<String, Comparator<Guest>> SORT_COMPARATORS = Map.of(
            "passport", Comparator.comparing(Guest::passportNumber),
            "lastName", Comparator.comparing(Guest::lastName),
            "birthDate", Comparator.comparing(Guest::birthDate)
    );

    private final GuestRepository repository;
    private final JsonSupport json;

    public GuestServlet(GuestRepository repository, JsonSupport json) {
        this.repository = repository;
        this.json = json;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RequestHelper.RouteTarget target = RequestHelper.parseRoute(req);

        switch (target) {
            case RequestHelper.RouteTarget.Collection ignored -> handleGetCollection(req, resp);
            case RequestHelper.RouteTarget.Stats ignored -> handleGetStats(resp);
            case RequestHelper.RouteTarget.Item(String id) -> handleGetById(id, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RequestHelper.RouteTarget target = RequestHelper.parseRoute(req);
        if (!(target instanceof RequestHelper.RouteTarget.Collection)) {
            throw new MethodNotAllowedException(
                    "Метод POST дозволений лише для колекції /api/guests",
                    "POST is not allowed on " + req.getRequestURI()
            );
        }

        Guest guest = json.readValid(req, Guest.class);
        repository.add(guest);

        String id = repository.identityOf(guest);
        String encodedId = URLEncoder.encode(id, StandardCharsets.UTF_8);
        resp.setHeader("Location", req.getContextPath() + req.getServletPath() + "/" + encodedId);
        json.write(resp, HttpServletResponse.SC_CREATED, guest);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RequestHelper.RouteTarget target = RequestHelper.parseRoute(req);
        if (!(target instanceof RequestHelper.RouteTarget.Item(String id))) {
            throw new MethodNotAllowedException(
                    "Метод PUT вимагає номер паспорта в шляху",
                    "PUT is not allowed on " + req.getRequestURI()
            );
        }

        Guest guest = json.readValid(req, Guest.class);
        String bodyId = repository.identityOf(guest);
        if (!id.equals(bodyId)) {
            throw new BadRequestException(
                    "Паспорт у тілі (" + bodyId + ") не збігається з ідентифікатором у шляху (" + id + ")",
                    "Path ID '" + id + "' does not match body ID '" + bodyId + "'"
            );
        }

        if (!repository.replace(guest)) {
            throw new NotFoundException(
                    "Гостя з паспортом " + id + " не знайдено",
                    "Guest not found for PUT: " + id
            );
        }

        json.write(resp, HttpServletResponse.SC_OK, guest);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        RequestHelper.RouteTarget target = RequestHelper.parseRoute(req);
        if (!(target instanceof RequestHelper.RouteTarget.Item(String id))) {
            throw new MethodNotAllowedException(
                    "Метод DELETE вимагає номер паспорта в шляху",
                    "DELETE is not allowed on " + req.getRequestURI()
            );
        }

        if (!repository.removeByIdentity(id)) {
            throw new NotFoundException(
                    "Гостя з паспортом " + id + " не знайдено",
                    "Guest not found for DELETE: " + id
            );
        }

        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private void handleGetCollection(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RequestHelper.validateAllowedParams(req, ALLOWED_QUERY_PARAMS);

        String lastNameParam = req.getParameter("lastName");
        Integer minBirthYear = RequestHelper.parseIntParam(req.getParameter("minBirthYear"), "minBirthYear");
        Integer maxBirthYear = RequestHelper.parseIntParam(req.getParameter("maxBirthYear"), "maxBirthYear");

        if (minBirthYear != null && maxBirthYear != null && minBirthYear > maxBirthYear) {
            throw new BadRequestException(
                    "minBirthYear не може бути більшим за maxBirthYear",
                    "minBirthYear (" + minBirthYear + ") > maxBirthYear (" + maxBirthYear + ")"
            );
        }

        Comparator<Guest> comparator = RequestHelper.resolveComparator(req, SORT_COMPARATORS);

        List<Guest> baseList = (comparator != null)
                ? repository.findAllSorted(comparator)
                : repository.findAll();

        Set<String> allowedByLastName = (lastNameParam != null && !lastNameParam.isBlank())
                ? repository.searchByLastName(lastNameParam).stream().map(Guest::passportNumber).collect(Collectors.toSet())
                : null;

        List<Guest> filtered = baseList.stream()
                .filter(g -> allowedByLastName == null || allowedByLastName.contains(g.passportNumber()))
                .filter(g -> minBirthYear == null || g.birthDate().getYear() >= minBirthYear)
                .filter(g -> maxBirthYear == null || g.birthDate().getYear() <= maxBirthYear)
                .toList();

        List<Guest> result = RequestHelper.paginate(req, filtered);
        json.write(resp, HttpServletResponse.SC_OK, result);
    }

    private void handleGetById(String id, HttpServletResponse resp) throws IOException {
        Guest guest = repository.findByIdentity(id)
                .orElseThrow(() -> new NotFoundException(
                        "Гостя з паспортом " + id + " не знайдено",
                        "Guest not found: " + id
                ));
        json.write(resp, HttpServletResponse.SC_OK, guest);
    }

    private void handleGetStats(HttpServletResponse resp) throws IOException {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalGuests", repository.size());
        stats.put("countByBirthYear", repository.countGuestsByBirthYear());
        json.write(resp, HttpServletResponse.SC_OK, stats);
    }
}
