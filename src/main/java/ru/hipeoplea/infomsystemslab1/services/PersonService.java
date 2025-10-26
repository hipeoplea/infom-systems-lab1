package ru.hipeoplea.infomsystemslab1.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hipeoplea.infomsystemslab1.models.Person;
import ru.hipeoplea.infomsystemslab1.exeption.NotFoundException;
import ru.hipeoplea.infomsystemslab1.repository.PersonRepository;
import ru.hipeoplea.infomsystemslab1.websocket.WsHub;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

@Service
@Transactional
public class PersonService {
    private final PersonRepository personRepository;
    private final WsHub wsHub;

    public PersonService(PersonRepository personRepository, WsHub wsHub) {
        this.personRepository = personRepository;
        this.wsHub = wsHub;
    }

    public Person create(Person person) {
        Person saved = personRepository.save(person);
        broadcastAfterCommit("created", saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<Person> getById(Long id) {
        return personRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<Person> getAll(Pageable pageable) {
        return personRepository.findAll(pageable);
    }

    public Person update(Long id, Person updated) {
        Person existing = personRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Person not found"));

        existing.setName(updated.getName());
        existing.setEyeColor(updated.getEyeColor());
        existing.setHairColor(updated.getHairColor());
        existing.setNationality(updated.getNationality());
        existing.setPassportID(updated.getPassportID());
        existing.setLocation(updated.getLocation());

        Person saved = personRepository.save(existing);
        broadcastAfterCommit("updated", saved.getId());
        return saved;
    }

    public void delete(Long id) {
        Person existing = personRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Person not found"));
        personRepository.delete(existing);
        broadcastAfterCommit("deleted", id);
    }

    private void broadcastAfterCommit(String type, Long id) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() {
                    wsHub.broadcast("{\"type\":\"" + type + "\",\"id\":" + id + "}");
                }
            });
        } else {
            wsHub.broadcast("{\"type\":\"" + type + "\",\"id\":" + id + "}");
        }
    }
}
