package ru.hipeoplea.is.lab1.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.hipeoplea.is.lab1.exeption.NotFoundException;
import ru.hipeoplea.is.lab1.generated.api.PersonsApi;
import ru.hipeoplea.is.lab1.generated.model.PagePersonResponse;
import ru.hipeoplea.is.lab1.models.Person;
import ru.hipeoplea.is.lab1.services.PersonService;
import ru.hipeoplea.is.lab1.util.PageRequestFactory;
import ru.hipeoplea.is.lab1.websocket.WsHub;

@RestController
@RequiredArgsConstructor
public class PersonController implements PersonsApi {
    private final PersonService personService;
    private final WsHub wsHub;

    @Override
    public ResponseEntity<Person> createPerson(Person person) {
        Person saved = personService.create(person);
        broadcastAfterCommit("created", saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Override
    public ResponseEntity<Person> getPerson(Long id) {
        Person person = personService.getById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Person with id:" + id + "not found"));
        return ResponseEntity.ok(person);
    }

    @Override
    public ResponseEntity<PagePersonResponse> listPersons(
            Integer page, Integer pageSize, String filter, String sortBy,
            String sortDir) {
        Pageable pageable = PageRequestFactory.build(
                page, pageSize, sortBy, sortDir, this::sanitizeSort);

        Page<Person> res = personService.getAll(pageable);
        PagePersonResponse body = new PagePersonResponse(
                res.getContent(), res.getTotalElements());
        return ResponseEntity.ok(body);
    }

    private String sanitizeSort(String sortBy) {
        return switch (sortBy) {
            case "id", "name", "eyeColor", "hairColor",
                 "nationality", "passportID" -> sortBy;
            default -> "id";
        };
    }

    @Override
    public ResponseEntity<Person> updatePerson(Long id, Person person) {
        Person saved = personService.update(id, person);
        broadcastAfterCommit("updated", saved.getId());
        return ResponseEntity.ok(saved);
    }

    @Override
    public ResponseEntity<Void> deletePerson(Long id) {
        personService.delete(id);
        broadcastAfterCommit("deleted", id);
        return ResponseEntity.noContent().build();
    }

    private void broadcastAfterCommit(String type, Long id) {
        wsHub.broadcast(
                "{\"type\":\"" + type + "\",\"id\":" + id + "}");
    }
}
