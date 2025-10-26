package ru.hipeoplea.infomsystemslab1.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.hipeoplea.infomsystemslab1.models.Coordinates;
import ru.hipeoplea.infomsystemslab1.exeption.NotFoundException;
import ru.hipeoplea.infomsystemslab1.exeption.BadRequestException;
import ru.hipeoplea.infomsystemslab1.services.CoordinatesService;

@RestController
@RequestMapping("/api/coordinates")
@RequiredArgsConstructor
public class CoordinatesController {
    private final CoordinatesService coordinatesService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Coordinates create(@RequestBody Coordinates coordinates) {
        return coordinatesService.create(coordinates);
    }

    @GetMapping("/{id}")
    public Coordinates getById(@PathVariable Long id) {
        return coordinatesService.getById(id).orElseThrow(() -> new NotFoundException("Coordinates not found"));
    }

    @GetMapping
    public Object getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "") String filter,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir
    ) {
        if (page < 1) throw new BadRequestException("page must be >= 1");
        if (pageSize < 1) throw new BadRequestException("pageSize must be >= 1");
        int p = Math.max(0, page - 1);
        Sort sort = Sort.by("desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC, sanitizeSort(sortBy));
        Pageable pageable = PageRequest.of(p, Math.max(1, pageSize), sort);

        Page<Coordinates> res = coordinatesService.getAll(pageable);
        return java.util.Map.of(
                "items", res.getContent(),
                "total", res.getTotalElements()
        );
    }

    private String sanitizeSort(String sortBy) {
        return switch (sortBy) {
            case "id", "x", "y" -> sortBy;
            default -> "id";
        };
    }

    @PutMapping("/{id}")
    public Coordinates update(@PathVariable Long id, @RequestBody Coordinates coordinates) {
        return coordinatesService.update(id, coordinates);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        coordinatesService.delete(id);
    }
}
