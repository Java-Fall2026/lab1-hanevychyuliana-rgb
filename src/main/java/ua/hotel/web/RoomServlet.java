package ua.hotel.web;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ua.hotel.exception.BadRequestException;
import ua.hotel.exception.MethodNotAllowedException;
import ua.hotel.exception.NotFoundException;
import ua.hotel.model.Room;
import ua.hotel.model.RoomType;
import ua.hotel.repository.RoomRepository;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RoomServlet extends HttpServlet {

    private static final Set<String> ALLOWED_QUERY_PARAMS = Set.of(
            "type", "minPrice", "maxPrice", "sort", "order", "limit", "offset"
    );

    private static final Map<String, Comparator<Room>> SORT_COMPARATORS = Map.of(
            "number", Comparator.comparingInt(Room::getRoomNumber),
            "price", Comparator.comparingDouble(Room::getPricePerNight),
            "type", Comparator.comparing(r -> r.getType().name())
    );

    private final RoomRepository repository;
    private final JsonSupport json;

    public RoomServlet(RoomRepository repository, JsonSupport json) {
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
                    "Метод POST дозволений лише для колекції /api/rooms",
                    "POST is not allowed on " + req.getRequestURI()
            );
        }

        Room room = json.readValid(req, Room.class);
        repository.add(room);

        String id = repository.identityOf(room);
        String encodedId = URLEncoder.encode(id, StandardCharsets.UTF_8);
        resp.setHeader("Location", req.getContextPath() + req.getServletPath() + "/" + encodedId);
        json.write(resp, HttpServletResponse.SC_CREATED, room);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RequestHelper.RouteTarget target = RequestHelper.parseRoute(req);
        if (!(target instanceof RequestHelper.RouteTarget.Item(String id))) {
            throw new MethodNotAllowedException(
                    "Метод PUT вимагає ідентифікатор кімнати в шляху",
                    "PUT is not allowed on " + req.getRequestURI()
            );
        }

        Room room = json.readValid(req, Room.class);
        String bodyId = repository.identityOf(room);
        if (!id.equals(bodyId)) {
            throw new BadRequestException(
                    "Номер кімнати в тілі (" + bodyId + ") не збігається з ідентифікатором у шляху (" + id + ")",
                    "Path ID '" + id + "' does not match body ID '" + bodyId + "'"
            );
        }

        if (!repository.replace(room)) {
            throw new NotFoundException(
                    "Кімнату з номером " + id + " не знайдено",
                    "Room not found for PUT: " + id
            );
        }

        json.write(resp, HttpServletResponse.SC_OK, room);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        RequestHelper.RouteTarget target = RequestHelper.parseRoute(req);
        if (!(target instanceof RequestHelper.RouteTarget.Item(String id))) {
            throw new MethodNotAllowedException(
                    "Метод DELETE вимагає ідентифікатор кімнати в шляху",
                    "DELETE is not allowed on " + req.getRequestURI()
            );
        }

        if (!repository.removeByIdentity(id)) {
            throw new NotFoundException(
                    "Кімнату з номером " + id + " не знайдено",
                    "Room not found for DELETE: " + id
            );
        }

        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private void handleGetCollection(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RequestHelper.validateAllowedParams(req, ALLOWED_QUERY_PARAMS);

        String typeParam = req.getParameter("type");
        Double minPriceParam = RequestHelper.parseDoubleParam(req.getParameter("minPrice"), "minPrice");
        Double maxPriceParam = RequestHelper.parseDoubleParam(req.getParameter("maxPrice"), "maxPrice");

        if (minPriceParam != null && maxPriceParam != null && minPriceParam > maxPriceParam) {
            throw new BadRequestException(
                    "minPrice не може бути більшим за maxPrice",
                    "minPrice (" + minPriceParam + ") > maxPrice (" + maxPriceParam + ")"
            );
        }

        Comparator<Room> comparator = RequestHelper.resolveComparator(req, SORT_COMPARATORS);

        List<Room> baseList = (comparator != null)
                ? repository.findAllSorted(comparator)
                : repository.findAll();

        Set<Integer> allowedByType = (typeParam != null && !typeParam.isBlank())
                ? repository.searchByType(typeParam).stream().map(Room::getRoomNumber).collect(Collectors.toSet())
                : null;

        Set<Integer> allowedByRange = (minPriceParam != null || maxPriceParam != null)
                ? repository.searchByPriceRange(
                        minPriceParam != null ? minPriceParam : 0.0,
                        maxPriceParam != null ? maxPriceParam : Double.MAX_VALUE
                ).stream().map(Room::getRoomNumber).collect(Collectors.toSet())
                : null;

        List<Room> filtered = baseList.stream()
                .filter(r -> allowedByType == null || allowedByType.contains(r.getRoomNumber()))
                .filter(r -> allowedByRange == null || allowedByRange.contains(r.getRoomNumber()))
                .toList();

        List<Room> result = RequestHelper.paginate(req, filtered);
        json.write(resp, HttpServletResponse.SC_OK, result);
    }

    private void handleGetById(String id, HttpServletResponse resp) throws IOException {
        Room room = repository.findByIdentity(id)
                .orElseThrow(() -> new NotFoundException(
                        "Кімнату з номером " + id + " не знайдено",
                        "Room not found: " + id
                ));
        json.write(resp, HttpServletResponse.SC_OK, room);
    }

    private void handleGetStats(HttpServletResponse resp) throws IOException {
        Map<RoomType, List<Room>> grouped = repository.groupRoomsByType();
        Map<String, Integer> countByType = new LinkedHashMap<>();
        grouped.forEach((type, rooms) -> countByType.put(type.name(), rooms.size()));

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalRooms", repository.size());
        stats.put("averagePrice", repository.getAveragePrice());
        stats.put("countByType", countByType);

        json.write(resp, HttpServletResponse.SC_OK, stats);
    }
}
