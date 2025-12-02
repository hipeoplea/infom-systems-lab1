package ru.hipeoplea.is.lab1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.hipeoplea.is.lab1.models.Color;
import ru.hipeoplea.is.lab1.models.Country;
import ru.hipeoplea.is.lab1.models.Person;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    boolean existsByNameAndEyeColorAndNationality(
            String name, Color eyeColor, Country nationality);

    boolean existsByNameAndEyeColorAndNationalityAndIdNot(
            String name, Color eyeColor, Country nationality, Long id);
}
