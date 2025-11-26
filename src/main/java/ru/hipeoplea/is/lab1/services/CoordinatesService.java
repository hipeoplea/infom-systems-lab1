package ru.hipeoplea.is.lab1.services;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import ru.hipeoplea.is.lab1.exeption.BadRequestException;
import ru.hipeoplea.is.lab1.exeption.NotFoundException;
import ru.hipeoplea.is.lab1.models.Coordinates;
import ru.hipeoplea.is.lab1.repository.CoordinatesRepository;
import ru.hipeoplea.is.lab1.web.GlobalExceptionHandler;
import ru.hipeoplea.is.lab1.validation.CoordinatesValidator;

@Service
@Transactional
public class CoordinatesService {
    private final CoordinatesRepository coordinatesRepository;
    private final CoordinatesValidator coordinatesValidator;

    public CoordinatesService(CoordinatesRepository coordinatesRepository,
            CoordinatesValidator coordinatesValidator) {
        this.coordinatesRepository = coordinatesRepository;
        this.coordinatesValidator = coordinatesValidator;
    }

    /**
     * Persists new coordinates.
     */
    public Coordinates create(Coordinates coordinates) {
        coordinatesValidator.validate(
                coordinates.getX(), coordinates.getY(), null);
        return coordinatesRepository.save(coordinates);
    }

    /**
     * Finds coordinates by id.
     */
    @Transactional(readOnly = true)
    public Optional<Coordinates> getById(Long id) {
        return coordinatesRepository.findById(id);
    }

    /**
     * Returns paginated coordinates.
     */
    @Transactional(readOnly = true)
    public Page<Coordinates> getAll(Pageable pageable) {
        return coordinatesRepository.findAll(pageable);
    }

    /**
     * Updates coordinates by id.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Coordinates update(Long id, Coordinates updated) {
        Coordinates existing =
                coordinatesRepository.findById(id)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Coordinates not found"));

        coordinatesValidator.validate(updated.getX(), updated.getY(), id);

        existing.setX(updated.getX());
        existing.setY(updated.getY());

        return coordinatesRepository.save(existing);
    }

    /**
     * Deletes coordinates; throws when referenced by other entities.
     */
    public void delete(Long id) {
        Coordinates existing = coordinatesRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException("Coordinates not found"));
        try {
            coordinatesRepository.delete(existing);
        } catch (RuntimeException ex) {
            if (GlobalExceptionHandler.isForeignKeyViolation(ex)) {
                throw new BadRequestException(
                        "Невозможно удалить: запись связана "
                                + "с другими объектами");
            }
            throw ex;
        }
    }
}
