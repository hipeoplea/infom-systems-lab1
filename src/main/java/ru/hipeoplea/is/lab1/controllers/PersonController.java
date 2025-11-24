package ru.hipeoplea.is.lab1.controllers;

import lombok.RequiredArgsConstructor;
import ru.hipeoplea.is.lab1.exeption.BadRequestException;
import ru.hipeoplea.is.lab1.exeption.NotFoundException;
import ru.hipeoplea.is.lab1.models.Person;
import ru.hipeoplea.is.lab1.services.PersonService;
import ru.hipeoplea.is.lab1.websocket.WsHub;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
public class PersonController {
    private final PersonService personService;
    private final WsHub wsHub;

    /**
     * Creates a new person entry.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Person create(@RequestBody Person person) {
        Person saved = personService.create(person);
        broadcastAfterCommit("created", saved.getId());
        return saved;
    }

    /**
     * Retrieves a person by id or throws if not found.
     */
    @GetMapping("/{id}")
    public Person getById(@PathVariable Long id) {
        return personService.getById(id)
                .orElseThrow(() -> new NotFoundException("Person not found"));
    }

    /**
     * Returns a paginated list of persons.
     */
    @GetMapping
    public Object getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int pageSize,
            @RequestParam(required = false, defaultValue = "") String filter,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir
    ) {
        if (page < 1) {
            throw new BadRequestException("page must be >= 1");
        }
        if (pageSize < 1) {
            throw new BadRequestException("pageSize must be >= 1");
        }
        int p = Math.max(0, page - 1);
        Sort.Direction direction =
                "desc".equalsIgnoreCase(sortDir)
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sanitizeSort(sortBy));
        Pageable pageable = PageRequest.of(p, Math.max(1, pageSize), sort);

        // Basic listing (filter ignored for now)
        Page<Person> res = personService.getAll(pageable);
        return java.util.Map.of(
                "items", res.getContent(),
                "total", res.getTotalElements()
        );
    }

    /**
     * Normalizes sort fields to an allowed white list.
     */
    private String sanitizeSort(String sortBy) {
        return switch (sortBy) {
            case "id", "name", "eyeColor", "hairColor",
                    "nationality", "passportID" -> sortBy;
            default -> "id";
        };
    }

    /**
     * Updates an existing person.
     */
    @PutMapping("/{id}")
    public Person update(@PathVariable Long id, @RequestBody Person person) {
        Person saved = personService.update(id, person);
        broadcastAfterCommit("updated", saved.getId());
        return saved;
    }

    /**
     * Deletes a person by id.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        personService.delete(id);
        broadcastAfterCommit("deleted", id);
    }

    private void broadcastAfterCommit(String type, Long id) {
        wsHub.broadcast(
                "{\"type\":\"" + type + "\",\"id\":" + id + "}");
    }
}
