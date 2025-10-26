package ru.hipeoplea.infomsystemslab1.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class SpaForwardController {

    // Only forward non-file, non-API, non-WS routes to index.html
    // Excludes paths starting with "api" or "ws" from SPA forwarding
    @GetMapping({
            "/{path:^(?!api|ws).*$}",
            "/{path:^(?!api|ws).*$}/**"
    })
    public String forward(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.contains(".")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return "forward:/index.html";
    }
}
