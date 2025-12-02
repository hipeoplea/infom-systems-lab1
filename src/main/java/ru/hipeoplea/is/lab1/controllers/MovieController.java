package ru.hipeoplea.is.lab1.controllers;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.hipeoplea.is.lab1.exeption.NotFoundException;
import ru.hipeoplea.is.lab1.generated.api.MoviesApi;
import ru.hipeoplea.is.lab1.generated.model.PageMovieResponse;
import ru.hipeoplea.is.lab1.models.Movie;
import ru.hipeoplea.is.lab1.services.ImportService;
import ru.hipeoplea.is.lab1.services.MovieService;
import ru.hipeoplea.is.lab1.util.PageRequestFactory;
import ru.hipeoplea.is.lab1.websocket.WsHub;
import ru.hipeoplea.is.lab1.web.ImportResult;

@RestController
@RequiredArgsConstructor
public class MovieController implements MoviesApi {
    private final MovieService movieService;
    private final WsHub wsHub;
    private final ImportService importService;

    @Override
    public ResponseEntity<Movie> createMovie(Movie movie) {
        Movie saved = movieService.create(movie);
        broadcastAfterCommit("created", saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Override
    public ResponseEntity<Movie> getMovie(Long id) {
        Movie movie = movieService.getById(id)
                .orElseThrow(() -> new NotFoundException("Movie not found"));
        return ResponseEntity.ok(movie);
    }

    @Override
    public ResponseEntity<PageMovieResponse> listMovies(
            Integer page, Integer pageSize, String filter, String sortBy,
            String sortDir) {
        Pageable pageable = PageRequestFactory.build(
                page, pageSize, sortBy, sortDir, this::sanitizeSort);

        Page<Movie> res = (filter != null && !filter.isBlank())
                ? movieService.search(filter.trim(), pageable)
                : movieService.getAll(pageable);

        PageMovieResponse body = new PageMovieResponse(
                res.getContent(), res.getTotalElements());
        return ResponseEntity.ok(body);
    }

    private String sanitizeSort(String sortBy) {
        return switch (sortBy) {
            case "id", "name", "goldenPalmCount", "usaBoxOffice", "length",
                    "totalBoxOffice", "budget", "creationDate", "genre",
                    "mpaaRating" -> sortBy;
            default -> "id";
        };
    }

    @Override
    public ResponseEntity<Movie> updateMovie(Long id, Movie movie) {
        Movie saved = movieService.update(id, movie);
        broadcastAfterCommit("updated", saved.getId());
        return ResponseEntity.ok(saved);
    }

    @Override
    public ResponseEntity<Void> deleteMovie(Long id) {
        movieService.delete(id);
        broadcastAfterCommit("deleted", id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Long> countMoviesByGoldenPalms(Long count) {
        long value = movieService.countByGoldenPalmCount(count);
        return ResponseEntity.ok(value);
    }

    @Override
    public ResponseEntity<Long> countMoviesByUsaBoxOffice(Long value) {
        long total = movieService.countByUsaBoxOfficeGreaterThan(value);
        return ResponseEntity.ok(total);
    }

    @Override
    public ResponseEntity<List<Movie>> listMoviesWithGenre() {
        return ResponseEntity.ok(movieService.findDistinctByGenreIsNotNull());
    }

    @Override
    public ResponseEntity<ImportResult> importMovies(
            String user, MultipartFile file) {
        ImportResult result = importService.importMovies(file, user);
        return ResponseEntity.ok(result);
    }

    private void broadcastAfterCommit(String type, Long id) {
        wsHub.broadcast(
                "{\"type\":\"" + type + "\",\"id\":" + id + "}");
    }
}
