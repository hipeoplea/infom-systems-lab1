package ru.hipeoplea.is.lab1.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import ru.hipeoplea.is.lab1.exeption.BadRequestException;
import ru.hipeoplea.is.lab1.models.Coordinates;
import ru.hipeoplea.is.lab1.models.ImportOperation;
import ru.hipeoplea.is.lab1.models.Location;
import ru.hipeoplea.is.lab1.models.Movie;
import ru.hipeoplea.is.lab1.models.Person;
import ru.hipeoplea.is.lab1.repository.CoordinatesRepository;
import ru.hipeoplea.is.lab1.repository.ImportOperationRepository;
import ru.hipeoplea.is.lab1.repository.LocationRepository;
import ru.hipeoplea.is.lab1.repository.MovieRepository;
import ru.hipeoplea.is.lab1.repository.PersonRepository;
import ru.hipeoplea.is.lab1.web.ImportResult;
import ru.hipeoplea.is.lab1.models.ImportStatus;
import ru.hipeoplea.is.lab1.validation.CoordinatesValidator;
import ru.hipeoplea.is.lab1.validation.LocationValidator;
import ru.hipeoplea.is.lab1.validation.PersonValidator;

@Service
@RequiredArgsConstructor
public class ImportService {
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final TransactionTemplate txTemplate;
    private final MovieRepository movieRepository;
    private final CoordinatesRepository coordinatesRepository;
    private final PersonRepository personRepository;
    private final LocationRepository locationRepository;
    private final ImportOperationRepository importOperationRepository;
    private final CoordinatesValidator coordinatesValidator;
    private final LocationValidator locationValidator;
    private final PersonValidator personValidator;
    /**
     * Imports movies from JSON file. All-or-nothing transaction.
     */
    public ImportResult importMovies(MultipartFile file, String user) {
        if (user == null || user.isBlank()) {
            throw new BadRequestException("Имя пользователя обязательно");
        }
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Файл не передан или пустой");
        }

        ImportOperation op = new ImportOperation(ImportStatus.IN_PROGRESS,
                user.trim());
        op = importOperationRepository.save(op);

        ImportOperation finalOp = op;
        try {
            ImportResult result = txTemplate.execute(status -> {
                List<Movie> movies = parseMovies(file);
                if (movies.isEmpty()) {
                    throw new BadRequestException(
                            "В файле нет фильмов для импорта");
                }
                validateAll(movies);

                for (Movie movie : movies) {
                    stripIds(movie);
                    persistNested(movie);
                    movieRepository.save(movie);
                }
                ImportOperation saved = importOperationRepository.findById(
                        finalOp.getId()).orElse(finalOp);
                saved.setImportedCount(movies.size());
                saved.setStatus(ImportStatus.SUCCESS);
                importOperationRepository.save(saved);
                return new ImportResult(movies.size());
            });
            return result;
        } catch (RuntimeException ex) {
            ImportOperation saved = importOperationRepository.findById(
                    finalOp.getId()).orElse(finalOp);
            saved.setStatus(ImportStatus.FAILED);
            importOperationRepository.save(saved);
            throw ex;
        }
    }

    private List<Movie> parseMovies(MultipartFile file) {
        try {
            return objectMapper.readValue(
                    file.getInputStream(),
                    new TypeReference<List<Movie>>() { });
        } catch (IOException e) {
            throw new BadRequestException(
                    "Не удалось прочитать JSON: " + e.getMessage());
        }
    }

    private void validateAll(List<Movie> movies) {
        List<String> errors = new ArrayList<>();
        java.util.Set<String> seenCoords = new java.util.HashSet<>();
        java.util.Set<String> seenLocations = new java.util.HashSet<>();
        java.util.Set<String> seenPersons = new java.util.HashSet<>();

        for (int i = 0; i < movies.size(); i++) {
            Movie movie = movies.get(i);
            String prefix = "movies[" + i + "]";

            collectViolations(errors, validator.validate(movie), prefix);

            if (movie.getCoordinates() != null) {
                collectViolations(errors,
                        validator.validate(movie.getCoordinates()),
                        prefix + ".coordinates");
                checkCoordinatesBusiness(
                        movie.getCoordinates(), prefix + ".coordinates",
                        errors, seenCoords);
            }
            if (movie.getDirector() != null) {
                checkPersonBusiness(movie.getDirector(),
                        prefix + ".director", errors,
                        seenPersons, seenLocations);
            }
            if (movie.getScreenwriter() != null) {
                checkPersonBusiness(movie.getScreenwriter(),
                        prefix + ".screenwriter", errors, seenPersons,
                        seenLocations);
            }
            if (movie.getOperator() != null) {
                checkPersonBusiness(movie.getOperator(),
                        prefix + ".operator", errors, seenPersons,
                        seenLocations);
            }
        }

        if (!errors.isEmpty()) {
            throw new BadRequestException(String.join("; ", errors));
        }
    }

    private void checkPersonBusiness(
            Person person,
            String prefix,
            List<String> errors,
            java.util.Set<String> seenPersons,
            java.util.Set<String> seenLocations) {
        collectViolations(errors, validator.validate(person), prefix);
        if (person.getLocation() != null) {
            collectViolations(errors, validator.validate(person.getLocation()),
                    prefix + ".location");
            validateLocationBusiness(person.getLocation(),
                    prefix + ".location", errors, seenLocations);
        }

        if (person.getName() != null
                && person.getEyeColor() != null
                && person.getNationality() != null) {
            String key = person.getName().trim() + "|"
                    + person.getEyeColor().name() + "|"
                    + person.getNationality().name();
            if (seenPersons.contains(key)) {
                errors.add(prefix + ": персона с такими данными повторяется "
                        + "в файле импорта");
            } else {
                seenPersons.add(key);
            }
            tryValidate(() -> personValidator.ensureUnique(
                    person.getName(), person.getEyeColor(),
                    person.getNationality(), null), prefix, errors);
        }
    }

    private void validateLocationBusiness(
            Location loc,
            String prefix,
            List<String> errors,
            java.util.Set<String> seenLocations) {
        try {
            locationValidator.validate(loc, null);
        } catch (BadRequestException ex) {
            errors.add(prefix + ": " + ex.getMessage());
            return;
        }

        String key = loc.getName().trim() + "|"
                + loc.getX() + "|" + loc.getY();
        if (seenLocations.contains(key)) {
            errors.add(prefix + ": такая локация уже есть в файле");
        } else {
            seenLocations.add(key);
        }
    }

    private void checkCoordinatesBusiness(
            Coordinates coordinates,
            String prefix,
            List<String> errors,
            java.util.Set<String> seenCoords) {
        Float x = coordinates.getX();
        Double y = coordinates.getY();
        if (x != null && y != null) {
            String key = x + "|" + y;
            if (seenCoords.contains(key)) {
                errors.add(prefix + ": координаты дублируются в файле");
            } else {
                seenCoords.add(key);
            }
            tryValidate(() -> coordinatesValidator.validate(x, y, null),
                    prefix, errors);
        }
    }

    private void tryValidate(Runnable action, String prefix,
            List<String> errors) {
        try {
            action.run();
        } catch (BadRequestException ex) {
            errors.add(prefix + ": " + ex.getMessage());
        }
    }

    private void collectViolations(
            List<String> errors,
            Set<? extends ConstraintViolation<?>> violations,
            String prefix) {
        for (ConstraintViolation<?> v : violations) {
            errors.add(prefix + "." + v.getPropertyPath() + ": "
                    + v.getMessage());
        }
    }

    private void stripIds(Movie movie) {
        movie.setId(null);

        Coordinates coordinates = movie.getCoordinates();
        if (coordinates != null) {
            coordinates.setId(null);
        }

        Person director = movie.getDirector();
        if (director != null) {
            director.setId(null);
            stripLocationId(director);
        }
        Person screenwriter = movie.getScreenwriter();
        if (screenwriter != null) {
            screenwriter.setId(null);
            stripLocationId(screenwriter);
        }
        Person operator = movie.getOperator();
        if (operator != null) {
            operator.setId(null);
            stripLocationId(operator);
        }
    }

    private void stripLocationId(Person person) {
        if (person.getLocation() != null) {
            person.getLocation().setId(null);
        }
    }

    private void persistNested(Movie movie) {
        if (movie.getCoordinates() != null) {
            movie.setCoordinates(
                    coordinatesRepository.save(movie.getCoordinates()));
        }

        movie.setDirector(savePerson(movie.getDirector()));
        movie.setScreenwriter(savePerson(movie.getScreenwriter()));
        movie.setOperator(savePerson(movie.getOperator()));
    }

    private Person savePerson(Person person) {
        if (person == null) {
            return null;
        }
        if (person.getLocation() != null) {
            person.setLocation(locationRepository.save(person.getLocation()));
        }
        return personRepository.save(person);
    }
}
