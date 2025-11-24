package ru.hipeoplea.is.lab1.controllers;

import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hipeoplea.is.lab1.models.Movie;
import ru.hipeoplea.is.lab1.models.MovieGenre;
import ru.hipeoplea.is.lab1.models.MpaaRating;
import ru.hipeoplea.is.lab1.repository.MovieRepository;

@RestController
@RequestMapping("/api/ops")
@RequiredArgsConstructor
public class OpsController {
    private final MovieRepository movieRepository;

    /**
     * Counts movies by the Golden Palm field.
     */
    @GetMapping("/goldenPalmCount")
    public long countGoldenPalm(@RequestParam("value") Long value) {
        return movieRepository.countByGoldenPalmCount(value);
    }

    /**
     * Counts movies where US box office exceeds the provided value.
     */
    @GetMapping("/usaBoxOfficeGreater")
    public long countUsaBoxOfficeGreater(@RequestParam("value") long value) {
        return movieRepository.countByUsaBoxOfficeGreaterThan(value);
    }

    /**
     * Returns a sorted list of unique genre names.
     */
    @GetMapping("/uniqueGenres")
    public List<String> uniqueGenres() {
        return movieRepository.findDistinctByGenreIsNotNull().stream()
                .map(Movie::getGenre)
                .map(Enum::name)
                .distinct()
                .sorted()
                .toList();
    }

    /**
     * Increments oscars for movies with MPAA rating R.
     */
    @PostMapping("/addOscarToR")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void addOscarToR() {
        movieRepository.incrementOscarsByRating(MpaaRating.R);
    }

    /**
     * Resets oscars for directors whose movies match the provided genre.
     */
    @PostMapping("/removeOscarsByDirectorsGenre")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void removeOscarsByDirectorsGenre(@RequestBody ParamGenre payload) {
        MovieGenre genre = MovieGenre.valueOf(payload.genre);
        List<Long> directorIds = movieRepository.findDirectorIdsByGenre(genre);
        if (directorIds == null || directorIds.isEmpty()) {
            return;
        }
        movieRepository.resetOscarsByDirectorIds(directorIds);
    }

    public record ParamGenre(String genre) { }
}
