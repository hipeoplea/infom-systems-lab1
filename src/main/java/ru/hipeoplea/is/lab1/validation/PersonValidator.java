package ru.hipeoplea.is.lab1.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.hipeoplea.is.lab1.exeption.BadRequestException;
import ru.hipeoplea.is.lab1.models.Color;
import ru.hipeoplea.is.lab1.models.Country;
import ru.hipeoplea.is.lab1.repository.PersonRepository;

@Component
@RequiredArgsConstructor
public class PersonValidator {
    private final PersonRepository personRepository;

    public void ensureUnique(String name, Color eyeColor,
            Country nationality, Long ignoreId) {
        if (name == null || name.isBlank()
                || eyeColor == null || nationality == null) {
            return;
        }
        boolean exists = (ignoreId == null)
                ? personRepository.existsByNameAndEyeColorAndNationality(
                        name.trim(), eyeColor, nationality)
                : personRepository
                        .existsByNameAndEyeColorAndNationalityAndIdNot(
                                name.trim(), eyeColor, nationality, ignoreId);
        if (exists) {
            throw new BadRequestException(
                    "Персона с таким именем, "
                            + "цветом глаз и гражданством уже есть");
        }
    }
}
