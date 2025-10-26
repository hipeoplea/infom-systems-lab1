package ru.hipeoplea.infomsystemslab1.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hipeoplea.infomsystemslab1.models.Location;
import ru.hipeoplea.infomsystemslab1.exeption.NotFoundException;
import ru.hipeoplea.infomsystemslab1.repository.LocationRepository;
import ru.hipeoplea.infomsystemslab1.websocket.WsHub;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

@Service
@Transactional
public class LocationService {
    private final LocationRepository locationRepository;
    private final WsHub wsHub;

    public LocationService(LocationRepository locationRepository, WsHub wsHub) {
        this.locationRepository = locationRepository;
        this.wsHub = wsHub;
    }

    public Location create(Location location) {
        Location saved = locationRepository.save(location);
        broadcastAfterCommit("created", saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<Location> getById(Long id) {
        return locationRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<Location> getAll(Pageable pageable) {
        return locationRepository.findAll(pageable);
    }

    public Location update(Long id, Location updated) {
        Location existing = locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Location not found"));

        existing.setX(updated.getX());
        existing.setY(updated.getY());
        existing.setName(updated.getName());

        Location saved = locationRepository.save(existing);
        broadcastAfterCommit("updated", saved.getId());
        return saved;
    }

    public void delete(Long id) {
        Location existing = locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Location not found"));
        locationRepository.delete(existing);
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
