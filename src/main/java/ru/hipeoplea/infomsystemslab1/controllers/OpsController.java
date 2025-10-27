package ru.hipeoplea.infomsystemslab1.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.hipeoplea.infomsystemslab1.models.Movie;
import ru.hipeoplea.infomsystemslab1.models.MovieGenre;
import ru.hipeoplea.infomsystemslab1.models.MpaaRating;
import ru.hipeoplea.infomsystemslab1.repository.MovieRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ops")
@RequiredArgsConstructor
public class OpsController {
    private final MovieRepository movieRepository;

    @GetMapping("/goldenPalmCount")
    public long countGoldenPalm(@RequestParam("value") Long value) {
        return movieRepository.countByGoldenPalmCount(value);
    }

    @GetMapping("/usaBoxOfficeGreater")
    public long countUsaBoxOfficeGreater(@RequestParam("value") long value) {
        return movieRepository.countByUsaBoxOfficeGreaterThan(value);
    }

    @GetMapping("/uniqueGenres")
    public List<String> uniqueGenres() {
        return movieRepository.findDistinctByGenreIsNotNull()
                .stream().map(Movie::getGenre).map(Enum::name)
                .distinct().sorted().toList();
    }

    @PostMapping("/addOscarToR")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void addOscarToR() {
        movieRepository.incrementOscarsByRating(MpaaRating.R);
    }

    @PostMapping("/removeOscarsByDirectorsGenre")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void removeOscarsByDirectorsGenre(@RequestBody ParamGenre payload) {
        MovieGenre genre = MovieGenre.valueOf(payload.genre);
        List<Long> directorIds = movieRepository.findDirectorIdsByGenre(genre);
        if (directorIds == null || directorIds.isEmpty()) return;
        movieRepository.resetOscarsByDirectorIds(directorIds);
    }

    public record ParamGenre(String genre){}
}
