package ru.hipeoplea.is.lab1.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.hipeoplea.is.lab1.exeption.NotFoundException;

@Controller
public class SpaForwardController {

    /**
     * Forwards non-API requests to the SPA entry point; throws when the path
     * targets a static resource.
     */
    @GetMapping({
            "/{path:^(?!api|ws).*$}",
            "/{path:^(?!api|ws).*$}/**"
    })
    public String forward(
            HttpServletRequest request, @PathVariable String path) {
        String uri = request.getRequestURI();
        if (uri.contains(".")) {
            throw new NotFoundException("Resource not found");
        }
        return "forward:/index.html";
    }
}
