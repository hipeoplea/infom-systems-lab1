package ru.hipeoplea.is.lab1.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.hipeoplea.is.lab1.exeption.NotFoundException;
import ru.hipeoplea.is.lab1.generated.api.CoordinatesApi;
import ru.hipeoplea.is.lab1.generated.model.PageCoordinatesResponse;
import ru.hipeoplea.is.lab1.models.Coordinates;
import ru.hipeoplea.is.lab1.services.CoordinatesService;
import ru.hipeoplea.is.lab1.util.PageRequestFactory;
import ru.hipeoplea.is.lab1.websocket.WsHub;

@RestController
@RequiredArgsConstructor
public class CoordinatesController implements CoordinatesApi {
    private final CoordinatesService coordinatesService;
    private final WsHub wsHub;

    @Override
    public ResponseEntity<Coordinates> createCoordinates(
            Coordinates coordinates) {
        Coordinates saved = coordinatesService.create(coordinates);
        broadcastAfterCommit("created", saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Override
    public ResponseEntity<Coordinates> getCoordinates(Long id) {
        Coordinates coordinates = coordinatesService.getById(id)
                .orElseThrow(() ->
                        new NotFoundException("Coordinates not found"));
        return ResponseEntity.ok(coordinates);
    }

    @Override
    public ResponseEntity<PageCoordinatesResponse> listCoordinates(
            Integer page, Integer pageSize, String filter, String sortBy,
            String sortDir) {
        Pageable pageable = PageRequestFactory.build(
                page, pageSize, sortBy, sortDir, this::sanitizeSort);

        Page<Coordinates> res = coordinatesService.getAll(pageable);
        PageCoordinatesResponse body = new PageCoordinatesResponse(
                res.getContent(), res.getTotalElements());
        return ResponseEntity.ok(body);
    }

    private String sanitizeSort(String sortBy) {
        return switch (sortBy) {
            case "id", "x", "y" -> sortBy;
            default -> "id";
        };
    }

    @Override
    public ResponseEntity<Coordinates> updateCoordinates(
            Long id, Coordinates coordinates) {
        Coordinates saved = coordinatesService.update(id, coordinates);
        broadcastAfterCommit("updated", saved.getId());
        return ResponseEntity.ok(saved);
    }

    @Override
    public ResponseEntity<Void> deleteCoordinates(Long id) {
        coordinatesService.delete(id);
        broadcastAfterCommit("deleted", id);
        return ResponseEntity.noContent().build();
    }

    private void broadcastAfterCommit(String type, Long id) {
        wsHub.broadcast(
                "{\"type\":\"" + type + "\",\"id\":" + id + "}");
    }
}
