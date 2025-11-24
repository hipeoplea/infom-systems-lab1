package ru.hipeoplea.is.lab1.controllers;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.hipeoplea.is.lab1.exeption.BadRequestException;
import ru.hipeoplea.is.lab1.models.MovieGenre;
import ru.hipeoplea.is.lab1.services.OpsService;

@RestController
@RequestMapping("/api/ops")
@RequiredArgsConstructor
public class OpsController {
    private final OpsService opsService;

    /**
     * Counts movies by the Golden Palm field.
     */
    @GetMapping("/goldenPalmCount")
    public long countGoldenPalm(@RequestParam("value") Long value) {
        return opsService.countGoldenPalm(value);
    }

    /**
     * Counts movies where US box office exceeds the provided value.
     */
    @GetMapping("/usaBoxOfficeGreater")
    public long countUsaBoxOfficeGreater(@RequestParam("value") long value) {
        return opsService.countUsaBoxOfficeGreater(value);
    }

    /**
     * Returns a sorted list of unique genre names.
     */
    @GetMapping("/uniqueGenres")
    public List<String> uniqueGenres() {
        return opsService.uniqueGenres();
    }

    /**
     * Increments oscars for movies with MPAA rating R.
     */
    @PostMapping("/addOscarToR")
    @ResponseStatus(HttpStatus.OK)
    public void addOscarToR() {
        opsService.addOscarToR();
    }

    /**
     * Resets oscars for directors whose movies match the provided genre.
     */
    @PostMapping("/removeOscarsByDirectorsGenre")
    @ResponseStatus(HttpStatus.OK)
    public void removeOscarsByDirectorsGenre(@RequestBody ParamGenre payload) {
        MovieGenre genre = Optional.ofNullable(payload)
                .map(ParamGenre::genre)
                .map(MovieGenre::valueOf)
                .orElseThrow(
                        () -> new BadRequestException("genre is required"));

        opsService.removeOscarsByDirectorsGenre(genre);
    }

    public record ParamGenre(String genre) { }
}
