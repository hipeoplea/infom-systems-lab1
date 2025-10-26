package ru.hipeoplea.infomsystemslab1.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.hipeoplea.infomsystemslab1.models.Location;
import ru.hipeoplea.infomsystemslab1.exeption.NotFoundException;
import ru.hipeoplea.infomsystemslab1.exeption.BadRequestException;
import ru.hipeoplea.infomsystemslab1.services.LocationService;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Location create(@RequestBody Location location) {
        return locationService.create(location);
    }

    @GetMapping("/{id}")
    public Location getById(@PathVariable Long id) {
        return locationService.getById(id).orElseThrow(() -> new NotFoundException("Location not found"));
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

        Page<Location> res = locationService.getAll(pageable);
        return java.util.Map.of(
                "items", res.getContent(),
                "total", res.getTotalElements()
        );
    }

    private String sanitizeSort(String sortBy) {
        return switch (sortBy) {
            case "id", "x", "y", "name" -> sortBy;
            default -> "id";
        };
    }

    @PutMapping("/{id}")
    public Location update(@PathVariable Long id, @RequestBody Location location) {
        return locationService.update(id, location);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        locationService.delete(id);
    }
}
