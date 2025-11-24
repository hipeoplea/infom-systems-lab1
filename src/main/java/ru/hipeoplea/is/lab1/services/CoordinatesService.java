package ru.hipeoplea.is.lab1.services;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hipeoplea.is.lab1.exeption.BadRequestException;
import ru.hipeoplea.is.lab1.exeption.NotFoundException;
import ru.hipeoplea.is.lab1.models.Coordinates;
import ru.hipeoplea.is.lab1.repository.CoordinatesRepository;
import ru.hipeoplea.is.lab1.web.GlobalExceptionHandler;

@Service
@Transactional
public class CoordinatesService {
    private final CoordinatesRepository coordinatesRepository;

    public CoordinatesService(CoordinatesRepository coordinatesRepository) {
        this.coordinatesRepository = coordinatesRepository;
    }

    /**
     * Persists new coordinates.
     */
    public Coordinates create(Coordinates coordinates) {
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
    public Coordinates update(Long id, Coordinates updated) {
        Coordinates existing =
                coordinatesRepository.findById(id)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Coordinates not found"));

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
