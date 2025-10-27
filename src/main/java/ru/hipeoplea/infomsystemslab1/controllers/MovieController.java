package ru.hipeoplea.infomsystemslab1.controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.hipeoplea.infomsystemslab1.models.Movie;
import ru.hipeoplea.infomsystemslab1.exeption.NotFoundException;
import ru.hipeoplea.infomsystemslab1.exeption.BadRequestException;
import ru.hipeoplea.infomsystemslab1.services.MovieService;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {
    private final MovieService movieService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Movie create(@RequestBody Movie movie) {
        return movieService.create(movie);
    }

    @GetMapping("/{id}")
    public Movie getById(@PathVariable Long id) {
        return movieService.getById(id)
                .orElseThrow(() -> new NotFoundException("Movie not found"));
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

        Page<Movie> res = (filter != null && !filter.isBlank())
                ? movieService.search(filter.trim(), pageable)
                : movieService.getAll(pageable);

        return java.util.Map.of(
                "items", res.getContent(),
                "total", res.getTotalElements()
        );
    }

    private String sanitizeSort(String sortBy) {
        // Whitelist of sortable fields to avoid invalid sort paths
        return switch (sortBy) {
            case "id", "name", "goldenPalmCount", "usaBoxOffice", "length", "totalBoxOffice", "budget", "creationDate", "genre", "mpaaRating" -> sortBy;
            default -> "id";
        };
    }

    // UPDATE
    @PutMapping("/{id}")
    public Movie update(@PathVariable Long id, @RequestBody Movie movie) {
        return movieService.update(id, movie);
    }

    // DELETE
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        movieService.delete(id);
    }

    // Примеры кастомных методов
    @GetMapping("/count/golden-palms/{count}")
    public long countByGoldenPalm(@PathVariable Long count) {
        return movieService.countByGoldenPalmCount(count);
    }

    @GetMapping("/count/usa-box-office/{value}")
    public long countByBoxOffice(@PathVariable long value) {
        return movieService.countByUsaBoxOfficeGreaterThan(value);
    }

    @GetMapping("/genres")
    public List<Movie> getMoviesWithGenre() {
        return movieService.findDistinctByGenreIsNotNull();
    }
}
