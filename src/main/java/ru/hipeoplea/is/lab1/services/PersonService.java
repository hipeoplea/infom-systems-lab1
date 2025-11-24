package ru.hipeoplea.is.lab1.services;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hipeoplea.is.lab1.exeption.BadRequestException;
import ru.hipeoplea.is.lab1.exeption.NotFoundException;
import ru.hipeoplea.is.lab1.models.Person;
import ru.hipeoplea.is.lab1.repository.PersonRepository;
import ru.hipeoplea.is.lab1.web.GlobalExceptionHandler;

@Service
@Transactional
public class PersonService {
    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    /**
     * Persists a new person.
     */
    public Person create(Person person) {
        return personRepository.save(person);
    }

    /**
     * Retrieves a person by id.
     */
    @Transactional(readOnly = true)
    public Optional<Person> getById(Long id) {
        return personRepository.findById(id);
    }

    /**
     * Lists persons with pagination support.
     */
    @Transactional(readOnly = true)
    public Page<Person> getAll(Pageable pageable) {
        return personRepository.findAll(pageable);
    }

    /**
     * Updates a person.
     */
    public Person update(Long id, Person updated) {
        Person existing =
                personRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Person not found"));

        existing.setName(updated.getName());
        existing.setEyeColor(updated.getEyeColor());
        existing.setHairColor(updated.getHairColor());
        existing.setNationality(updated.getNationality());
        existing.setPassportID(updated.getPassportID());
        existing.setLocation(updated.getLocation());

        return personRepository.save(existing);
    }

    /**
     * Deletes a person; raises when referenced by other entities.
     */
    public void delete(Long id) {
        Person existing = personRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Person not found"));
        try {
            personRepository.delete(existing);
        } catch (RuntimeException ex) {
            if (GlobalExceptionHandler.isForeignKeyViolation(ex)) {
                throw new BadRequestException(
                        "Невозможно удалить: запись связана с фильмами "
                                + "или другими объектами");
            }
            throw ex;
        }
    }
}
