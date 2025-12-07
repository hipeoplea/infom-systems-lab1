package ru.hipeoplea.is.lab1.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import java.util.UUID;
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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;


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
    private final FileStorageService fileStorageService;
    public ImportResult importMovies(MultipartFile file, String user) {
        if (user == null || user.isBlank()) {
            throw new BadRequestException("Имя пользователя обязательно");
        }
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Файл не передан или пустой");
        }

        byte[] payload;
        try {
            payload = file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("Не удалось прочитать файл: "
                    + e.getMessage());
        }

        ImportOperation op = new ImportOperation(
                ImportStatus.IN_PROGRESS, user.trim());
        op.setFileName(file.getOriginalFilename());
        op.setFileSize((long) payload.length);
        op = importOperationRepository.save(op);

        ImportOperation finalOp = op;
        String tempKey = buildTempKey(op.getId(),
                file.getOriginalFilename());
        String finalKey = buildFinalKey(op.getId(),
                file.getOriginalFilename());
        String contentType = file.getContentType() == null
                ? "application/json" : file.getContentType();
        String storedTemp = null;
        try {
            storedTemp = fileStorageService.uploadImportFile(tempKey,
                    payload, contentType);
            final String tempStoredKey = storedTemp;
            final String finalStoredKey = finalKey;
            return txTemplate.execute(status -> {
                List<Movie> movies = parseMovies(payload);
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
                if (saved.getStatus() != ImportStatus.IN_PROGRESS) {
                    throw new IllegalStateException(
                            "Import operation already finalized with status "
                                    + saved.getStatus());
                }
                saved.setImportedCount(movies.size());
                saved.setStatus(ImportStatus.PREPARED);
                saved.setTempFileKey(tempStoredKey);
                importOperationRepository.save(saved);
                return new ImportResult(movies.size());
            });
        } catch (Exception ex) {
            if (storedTemp != null) {
                fileStorageService.deleteQuietly(storedTemp);
            }
            ImportOperation saved = importOperationRepository.findById(
                    finalOp.getId()).orElse(finalOp);
            if (saved.getStatus() == ImportStatus.IN_PROGRESS
                    || saved.getStatus() == ImportStatus.PREPARED) {
                saved.setStatus(ImportStatus.FAILED);
                importOperationRepository.save(saved);
            }
            throw (RuntimeException) ex;
        } finally {
            scheduleFinalize(finalOp.getId(), finalKey);
        }
    }

    private List<Movie> parseMovies(byte[] payload) {
        try {
            return objectMapper.readValue(
                    payload,
                    new TypeReference<List<Movie>>() { });
        } catch (IOException e) {
            String message = e.getMessage() == null
                    ? "unknown error"
                    : e.getMessage();
            throw new BadRequestException(
                    "Не удалось прочитать JSON: " + message);
        }
    }

    private void validateAll(List<Movie> movies) {
        List<String> errors = new ArrayList<>();
        Set<String> seenCoords = new HashSet<>();
        Set<String> seenLocations = new HashSet<>();
        Set<String> seenPersons = new HashSet<>();

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
            Set<String> seenPersons,
            Set<String> seenLocations) {
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
            Set<String> seenLocations) {
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
            Set<String> seenCoords) {
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

    private String buildTempKey(Long importId, String originalName) {
        String safeName = (originalName == null || originalName.isBlank())
                ? "upload.json"
                : originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        return "imports/" + importId + "/temp/" + UUID.randomUUID() + "-"
                + safeName;
    }

    private String buildFinalKey(Long importId, String originalName) {
        String safeName = (originalName == null || originalName.isBlank())
                ? "upload.json"
                : originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        return "imports/" + importId + "/" + safeName;
    }

    private void scheduleFinalize(Long importId, String finalKey) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            finalizeFile(importId, finalKey);
                        }
                    });
        } else {
            finalizeFile(importId, finalKey);
        }
    }

    public void retryFinalizeFile(Long importId) {
        finalizeFile(importId, buildFinalKey(importId, null));
    }

    private void finalizeFile(Long importId, String finalKey) {
        txTemplate.executeWithoutResult(status -> {
            importOperationRepository.findById(importId).ifPresent(op -> {
                if (op.getStatus() != ImportStatus.PREPARED
                        || op.getTempFileKey() == null) {
                    return;
                }
                try {
                    fileStorageService.copyObject(op.getTempFileKey(),
                            finalKey);
                    fileStorageService.deleteQuietly(op.getTempFileKey());
                    op.setFileKey(finalKey);
                    op.setTempFileKey(null);
                    op.setStatus(ImportStatus.SUCCESS);
                } catch (Exception e) {
                    op.setStatus(ImportStatus.FAILED);
                }
                importOperationRepository.save(op);
            });
        });
    }
}
