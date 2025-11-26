package ru.hipeoplea.is.lab1.controllers;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.hipeoplea.is.lab1.exeption.BadRequestException;
import ru.hipeoplea.is.lab1.generated.api.OperationsApi;
import ru.hipeoplea.is.lab1.generated.model.ParamGenre;
import ru.hipeoplea.is.lab1.models.MovieGenre;
import ru.hipeoplea.is.lab1.services.OpsService;

@RestController
@RequiredArgsConstructor
public class OpsController implements OperationsApi {
    private final OpsService opsService;

    @Override
    public ResponseEntity<Long> countGoldenPalm(Long value) {
        long total = opsService.countGoldenPalm(value);
        return ResponseEntity.ok(total);
    }

    @Override
    public ResponseEntity<Long> countUsaBoxOfficeGreater(Long value) {
        long total = opsService.countUsaBoxOfficeGreater(value);
        return ResponseEntity.ok(total);
    }

    @Override
    public ResponseEntity<List<String>> listUniqueGenres() {
        return ResponseEntity.ok(opsService.uniqueGenres());
    }

    @Override
    public ResponseEntity<Void> addOscarToR() {
        opsService.addOscarToR();
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> removeOscarsByDirectorsGenre(
            ParamGenre payload) {
        MovieGenre genre = Optional.ofNullable(payload)
                .map(ParamGenre::getGenre)
                .orElseThrow(
                        () -> new BadRequestException("genre is required"));

        opsService.removeOscarsByDirectorsGenre(genre);
        return ResponseEntity.ok().build();
    }
}
