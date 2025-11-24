package ru.hipeoplea.is.lab1.controllers;

import lombok.RequiredArgsConstructor;
import ru.hipeoplea.is.lab1.exeption.BadRequestException;
import ru.hipeoplea.is.lab1.exeption.NotFoundException;
import ru.hipeoplea.is.lab1.models.Coordinates;
import ru.hipeoplea.is.lab1.services.CoordinatesService;
import ru.hipeoplea.is.lab1.websocket.WsHub;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coordinates")
@RequiredArgsConstructor
public class CoordinatesController {
    private final CoordinatesService coordinatesService;
    private final WsHub wsHub;

    /**
     * Creates coordinates.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Coordinates create(@RequestBody Coordinates coordinates) {
        Coordinates saved = coordinatesService.create(coordinates);
        broadcastAfterCommit("created", saved.getId());
        return saved;
    }

    /**
     * Fetches coordinates by id or throws when not found.
     */
    @GetMapping("/{id}")
    public Coordinates getById(@PathVariable Long id) {
        return coordinatesService.getById(id)
                .orElseThrow(
                        () -> new NotFoundException("Coordinates not found"));
    }

    /**
     * Lists coordinates with pagination.
     */
    @GetMapping
    public Object getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "") String filter,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir
    ) {
        if (page < 1) {
            throw new BadRequestException("page must be >= 1");
        }
        if (pageSize < 1) {
            throw new BadRequestException("pageSize must be >= 1");
        }
        int p = Math.max(0, page - 1);
        Sort.Direction direction =
                "desc".equalsIgnoreCase(sortDir)
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sanitizeSort(sortBy));
        Pageable pageable = PageRequest.of(p, Math.max(1, pageSize), sort);

        Page<Coordinates> res = coordinatesService.getAll(pageable);
        return java.util.Map.of(
                "items", res.getContent(),
                "total", res.getTotalElements()
        );
    }

    /**
     * Allows only safe sort fields.
     */
    private String sanitizeSort(String sortBy) {
        return switch (sortBy) {
            case "id", "x", "y" -> sortBy;
            default -> "id";
        };
    }

    /**
     * Updates coordinates.
     */
    @PutMapping("/{id}")
    public Coordinates update(
            @PathVariable Long id, @RequestBody Coordinates coordinates) {
        Coordinates saved = coordinatesService.update(id, coordinates);
        broadcastAfterCommit("updated", saved.getId());
        return saved;
    }

    /**
     * Deletes coordinates by id.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        coordinatesService.delete(id);
        broadcastAfterCommit("deleted", id);
    }

    private void broadcastAfterCommit(String type, Long id) {
        wsHub.broadcast(
                "{\"type\":\"" + type + "\",\"id\":" + id + "}");
    }
}
