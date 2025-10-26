package ru.hipeoplea.infomsystemslab1.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.hipeoplea.infomsystemslab1.models.Person;
import ru.hipeoplea.infomsystemslab1.exeption.NotFoundException;
import ru.hipeoplea.infomsystemslab1.exeption.BadRequestException;
import ru.hipeoplea.infomsystemslab1.services.PersonService;

@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
public class PersonController {
    private final PersonService personService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Person create(@RequestBody Person person) {
        return personService.create(person);
    }

    @GetMapping("/{id}")
    public Person getById(@PathVariable Long id) {
        return personService.getById(id).orElseThrow(() -> new NotFoundException("Person not found"));
    }

    @GetMapping
    public Object getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "") String filter,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir
    ) {
        if (page < 1) throw new BadRequestException("page must be >= 1");
        if (pageSize < 1) throw new BadRequestException("pageSize must be >= 1");
        int p = Math.max(0, page - 1);
        Sort sort = Sort.by("desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC, sanitizeSort(sortBy));
        Pageable pageable = PageRequest.of(p, Math.max(1, pageSize), sort);

        // Basic listing (filter ignored for now)
        Page<Person> res = personService.getAll(pageable);
        return java.util.Map.of(
                "items", res.getContent(),
                "total", res.getTotalElements()
        );
    }

    private String sanitizeSort(String sortBy) {
        return switch (sortBy) {
            case "id", "name", "eyeColor", "hairColor", "nationality", "passportID" -> sortBy;
            default -> "id";
        };
    }

    @PutMapping("/{id}")
    public Person update(@PathVariable Long id, @RequestBody Person person) {
        return personService.update(id, person);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        personService.delete(id);
    }
}
