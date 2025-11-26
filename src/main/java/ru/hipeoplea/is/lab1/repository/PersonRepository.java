package ru.hipeoplea.is.lab1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.hipeoplea.is.lab1.models.Person;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
}

