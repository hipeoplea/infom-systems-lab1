package ru.hipeoplea.is.lab1.services;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import ru.hipeoplea.is.lab1.exeption.BadRequestException;
import ru.hipeoplea.is.lab1.exeption.NotFoundException;
import ru.hipeoplea.is.lab1.models.Location;
import ru.hipeoplea.is.lab1.repository.LocationRepository;
import ru.hipeoplea.is.lab1.web.GlobalExceptionHandler;
import ru.hipeoplea.is.lab1.validation.LocationValidator;

@Service
@Transactional
public class LocationService {
    private final LocationRepository locationRepository;
    private final LocationValidator locationValidator;

    public LocationService(LocationRepository locationRepository,
            LocationValidator locationValidator) {
        this.locationRepository = locationRepository;
        this.locationValidator = locationValidator;
    }

    /**
     * Creates a location.
     */
    public Location create(Location location) {
        locationValidator.validate(location, null);
        return locationRepository.save(location);
    }

    /**
     * Retrieves a location by id.
     */
    @Transactional(readOnly = true)
    public Optional<Location> getById(Long id) {
        return locationRepository.findById(id);
    }

    /**
     * Returns paginated locations.
     */
    @Transactional(readOnly = true)
    public Page<Location> getAll(Pageable pageable) {
        return locationRepository.findAll(pageable);
    }

    /**
     * Updates a location.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Location update(Long id, Location updated) {
        Location existing =
                locationRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Location not found"));

        locationValidator.validate(updated, id);

        existing.setX(updated.getX());
        existing.setY(updated.getY());
        existing.setName(updated.getName());

        return locationRepository.save(existing);
    }

    /**
     * Deletes a location; raises when referenced by other entities.
     */
    public void delete(Long id) {
        Location existing = locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Location not found"));
        try {
            locationRepository.delete(existing);
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
