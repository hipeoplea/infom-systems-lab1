package ru.hipeoplea.infomsystemslab1.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hipeoplea.infomsystemslab1.models.Coordinates;
import ru.hipeoplea.infomsystemslab1.exeption.NotFoundException;
import ru.hipeoplea.infomsystemslab1.repository.CoordinatesRepository;
import ru.hipeoplea.infomsystemslab1.websocket.WsHub;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

@Service
@Transactional
public class CoordinatesService {
    private final CoordinatesRepository coordinatesRepository;
    private final WsHub wsHub;

    public CoordinatesService(CoordinatesRepository coordinatesRepository, WsHub wsHub) {
        this.coordinatesRepository = coordinatesRepository;
        this.wsHub = wsHub;
    }

    public Coordinates create(Coordinates coordinates) {
        Coordinates saved = coordinatesRepository.save(coordinates);
        broadcastAfterCommit("created", saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<Coordinates> getById(Long id) {
        return coordinatesRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<Coordinates> getAll(Pageable pageable) {
        return coordinatesRepository.findAll(pageable);
    }

    public Coordinates update(Long id, Coordinates updated) {
        Coordinates existing = coordinatesRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Coordinates not found"));

        existing.setX(updated.getX());
        existing.setY(updated.getY());

        Coordinates saved = coordinatesRepository.save(existing);
        broadcastAfterCommit("updated", saved.getId());
        return saved;
    }

    public void delete(Long id) {
        Coordinates existing = coordinatesRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Coordinates not found"));
        coordinatesRepository.delete(existing);
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
