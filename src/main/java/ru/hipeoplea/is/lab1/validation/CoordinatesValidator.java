package ru.hipeoplea.is.lab1.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.hipeoplea.is.lab1.exeption.BadRequestException;
import ru.hipeoplea.is.lab1.repository.CoordinatesRepository;

@Component
@RequiredArgsConstructor
public class CoordinatesValidator {
    private final CoordinatesRepository coordinatesRepository;

    /**
     * Ensures coordinates pair (x, y) is unique.
     */
    public void validate(Float x, Double y, Long ignoreId) {
        if (x == null || y == null) {
            return;
        }
        boolean exists = (ignoreId == null)
                ? coordinatesRepository.existsByXAndY(x, y)
                : coordinatesRepository.existsByXAndYAndIdNot(x, y, ignoreId);
        if (exists) {
            throw new BadRequestException(
                    "Координаты с такими x и y уже существуют");
        }
    }
}
