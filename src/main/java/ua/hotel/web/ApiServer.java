package ua.hotel.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.DispatcherType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.eclipse.jetty.ee10.servlet.FilterHolder;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import ua.hotel.repository.GuestRepository;
import ua.hotel.repository.RoomRepository;
import ua.hotel.util.ObjectMapperFactory;

import java.util.EnumSet;

public class ApiServer {

    private final Server server;
    private final ValidatorFactory validatorFactory;

    public ApiServer(int port, RoomRepository rooms, GuestRepository guests) {
        this(port, rooms, guests, ObjectMapperFactory.create());
    }

    public ApiServer(int port, RoomRepository rooms, GuestRepository guests, ObjectMapper mapper) {
        this.server = new Server(port);
        this.validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();

        JsonSupport jsonSupport = new JsonSupport(mapper, validator);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");

        // Register outer logging filter first so it captures final status codes set by ErrorHandlingFilter
        context.addFilter(new FilterHolder(new RequestLoggingFilter()), "/*", EnumSet.of(DispatcherType.REQUEST));
        context.addFilter(new FilterHolder(new ErrorHandlingFilter(jsonSupport)), "/*", EnumSet.of(DispatcherType.REQUEST));

        // Register domain servlets and fallback 404 servlet
        context.addServlet(new ServletHolder("roomServlet", new RoomServlet(rooms, jsonSupport)), "/api/rooms/*");
        context.addServlet(new ServletHolder("guestServlet", new GuestServlet(guests, jsonSupport)), "/api/guests/*");
        context.addServlet(new ServletHolder("notFoundServlet", new NotFoundServlet()), "/*");

        server.setHandler(context);
    }

    public void start() throws Exception {
        server.start();
    }

    public void stop() throws Exception {
        try {
            server.stop();
        } finally {
            validatorFactory.close();
        }
    }

    public int port() {
        if (server.getConnectors().length > 0 && server.getConnectors()[0] instanceof ServerConnector connector) {
            return connector.getLocalPort();
        }
        throw new IllegalStateException("Server connector is not initialized");
    }
}
