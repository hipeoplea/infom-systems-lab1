package ru.hipeoplea.is.lab1.validation;

import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.hipeoplea.is.lab1.exeption.BadRequestException;
import ru.hipeoplea.is.lab1.models.Location;
import ru.hipeoplea.is.lab1.repository.LocationRepository;

@Component
@RequiredArgsConstructor
public class LocationValidator {
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z ]+$");
    private static final float MIN_LON = -180F;
    private static final float MAX_LON = 180F;
    private static final int MIN_LAT = -90;
    private static final int MAX_LAT = 90;
    private static final int MAX_NAME = 100;
    private final LocationRepository locationRepository;

    public void validate(Location loc, Long ignoreId) {
        if (loc == null) {
            throw new BadRequestException("Локация не может быть пустой");
        }
        validateName(loc.getName());
        validateRange(loc.getX(), loc.getY());
        ensureUnique(loc.getName(), loc.getX(), loc.getY(), ignoreId);
    }

    private void validateName(String name) {
        if (name == null) {
            throw new BadRequestException("Название локации обязательно");
        }
        String trimmed = name.trim();
        if (trimmed.length() < 2 || trimmed.length() > MAX_NAME) {
            throw new BadRequestException(
                    "Длина названия локации должна быть 2-100 символов");
        }
        if (!NAME_PATTERN.matcher(trimmed).matches()) {
            throw new BadRequestException(
                    "Название локации может содержать только буквы и пробелы");
        }
    }

    private void validateRange(Float x, Integer y) {
        if (x == null) {
            throw new BadRequestException("Координата x обязательна");
        }
        if (x < MIN_LON || x > MAX_LON) {
            throw new BadRequestException(
                    "Долгота (x) должна быть в диапазоне [-180; 180]");
        }
        if (y == null) {
            throw new BadRequestException("Координата y обязательна");
        }
        if (y < MIN_LAT || y > MAX_LAT) {
            throw new BadRequestException(
                    "Широта (y) должна быть в диапазоне [-90; 90]");
        }
    }

    private void ensureUnique(String name, Float x, Integer y, Long ignoreId) {
        if (name == null || x == null || y == null) {
            return;
        }
        boolean exists = (ignoreId == null)
                ? locationRepository.existsByNameAndXAndY(name.trim(), x, y)
                : locationRepository.existsByNameAndXAndYAndIdNot(
                        name.trim(), x, y, ignoreId);
        if (exists) {
            throw new BadRequestException(
                    "Локация с таким именем и координатами уже существует");
        }
    }
}
