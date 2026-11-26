package ua.hotel.web;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ua.hotel.exception.NotFoundException;

public class NotFoundServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        throw new NotFoundException(
                "Ресурс за шляхом " + req.getRequestURI() + " не знайдено",
                "No servlet mapped for path: " + req.getRequestURI()
        );
    }
}
