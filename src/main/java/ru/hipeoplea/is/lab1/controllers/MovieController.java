package ru.hipeoplea.is.lab1.controllers;

import java.util.List;
import lombok.RequiredArgsConstructor;
import ru.hipeoplea.is.lab1.exeption.NotFoundException;
import ru.hipeoplea.is.lab1.models.Movie;
import ru.hipeoplea.is.lab1.services.MovieService;
import ru.hipeoplea.is.lab1.websocket.WsHub;
import ru.hipeoplea.is.lab1.util.PageRequestFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {
    private final MovieService movieService;
    private final WsHub wsHub;

    /**
     * Creates a movie.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Movie create(@RequestBody Movie movie) {
        Movie saved = movieService.create(movie);
        broadcastAfterCommit("created", saved.getId());
        return saved;
    }

    /**
     * Gets movie by id.
     */
    @GetMapping("/{id}")
    public Movie getById(@PathVariable Long id) {
        return movieService.getById(id)
                .orElseThrow(() -> new NotFoundException("Movie not found"));
    }

    /**
     * Lists movies with optional search.
     */
    @GetMapping
    public Object getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "") String filter,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir
    ) {
        Pageable pageable = PageRequestFactory.build(
                page, pageSize, sortBy, sortDir, this::sanitizeSort);

        Page<Movie> res = (filter != null && !filter.isBlank())
                ? movieService.search(filter.trim(), pageable)
                : movieService.getAll(pageable);

        return java.util.Map.of(
                "items", res.getContent(),
                "total", res.getTotalElements()
        );
    }

    private String sanitizeSort(String sortBy) {
        return switch (sortBy) {
            case "id", "name", "goldenPalmCount", "usaBoxOffice", "length",
                    "totalBoxOffice", "budget", "creationDate", "genre",
                    "mpaaRating" -> sortBy;
            default -> "id";
        };
    }

    /**
     * Updates an existing movie.
     */
    @PutMapping("/{id}")
    public Movie update(@PathVariable Long id, @RequestBody Movie movie) {
        Movie saved = movieService.update(id, movie);
        broadcastAfterCommit("updated", saved.getId());
        return saved;
    }

    /**
     * Deletes a movie by id.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        movieService.delete(id);
        broadcastAfterCommit("deleted", id);
    }

    /**
     * Counts movies by golden palm.
     */
    @GetMapping("/count/golden-palms/{count}")
    public long countByGoldenPalm(@PathVariable Long count) {
        return movieService.countByGoldenPalmCount(count);
    }

    /**
     * Counts movies by USA box office threshold.
     */
    @GetMapping("/count/usa-box-office/{value}")
    public long countByBoxOffice(@PathVariable long value) {
        return movieService.countByUsaBoxOfficeGreaterThan(value);
    }

    /**
     * Returns movies with a non-null genre.
     */
    @GetMapping("/genres")
    public List<Movie> getMoviesWithGenre() {
        return movieService.findDistinctByGenreIsNotNull();
    }

    private void broadcastAfterCommit(String type, Long id) {
        wsHub.broadcast(
                "{\"type\":\"" + type + "\",\"id\":" + id + "}");
    }
}
