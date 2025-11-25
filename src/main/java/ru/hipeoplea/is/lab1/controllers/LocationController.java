package ru.hipeoplea.is.lab1.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.hipeoplea.is.lab1.exeption.NotFoundException;
import ru.hipeoplea.is.lab1.generated.api.LocationsApi;
import ru.hipeoplea.is.lab1.generated.model.PageLocationResponse;
import ru.hipeoplea.is.lab1.models.Location;
import ru.hipeoplea.is.lab1.services.LocationService;
import ru.hipeoplea.is.lab1.util.PageRequestFactory;
import ru.hipeoplea.is.lab1.websocket.WsHub;

@RestController
@RequiredArgsConstructor
public class LocationController implements LocationsApi {
    private final LocationService locationService;
    private final WsHub wsHub;

    @Override
    public ResponseEntity<Location> createLocation(Location location) {
        Location saved = locationService.create(location);
        broadcastAfterCommit("created", saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Override
    public ResponseEntity<Location> getLocation(Long id) {
        Location location = locationService
                .getById(id)
                .orElseThrow(() -> new NotFoundException("Location not found"));
        return ResponseEntity.ok(location);
    }

    @Override
    public ResponseEntity<PageLocationResponse> listLocations(
            Integer page, Integer pageSize, String filter, String sortBy,
            String sortDir) {
        Pageable pageable = PageRequestFactory.build(
                page, pageSize, sortBy, sortDir, this::sanitizeSort);

        Page<Location> res = locationService.getAll(pageable);
        PageLocationResponse body = new PageLocationResponse(
                res.getContent(), res.getTotalElements());
        return ResponseEntity.ok(body);
    }

    private String sanitizeSort(String sortBy) {
        return switch (sortBy) {
            case "id", "x", "y", "name" -> sortBy;
            default -> "id";
        };
    }

    @Override
    public ResponseEntity<Location> updateLocation(Long id, Location location) {
        Location saved = locationService.update(id, location);
        broadcastAfterCommit("updated", saved.getId());
        return ResponseEntity.ok(saved);
    }

    @Override
    public ResponseEntity<Void> deleteLocation(Long id) {
        locationService.delete(id);
        broadcastAfterCommit("deleted", id);
        return ResponseEntity.noContent().build();
    }

    private void broadcastAfterCommit(String type, Long id) {
        wsHub.broadcast(
                "{\"type\":\"" + type + "\",\"id\":" + id + "}");
    }
}
