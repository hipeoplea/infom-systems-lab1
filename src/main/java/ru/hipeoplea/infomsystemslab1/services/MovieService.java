package ru.hipeoplea.infomsystemslab1.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.annotation.Transactional;
import ru.hipeoplea.infomsystemslab1.models.Movie;
import ru.hipeoplea.infomsystemslab1.exeption.NotFoundException;
import ru.hipeoplea.infomsystemslab1.repository.MovieRepository;
import ru.hipeoplea.infomsystemslab1.websocket.WsHub;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MovieService {
    private final MovieRepository movieRepository;
    private final WsHub wsHub;

    public MovieService(MovieRepository movieRepository, WsHub wsHub) {
        this.movieRepository = movieRepository;
        this.wsHub = wsHub;
    }

    public Movie create(Movie movie) {
        Movie saved = movieRepository.save(movie);
        broadcastAfterCommit("created", saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<Movie> getById(Long id) {
        return movieRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<Movie> getAll(Pageable pageable) {
        return movieRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Movie> search(String q, Pageable pageable) {
        return movieRepository.search(q, pageable);
    }

    public Movie update(Long id, Movie updated) {
        Movie existing = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Movie not found"));

        existing.setName(updated.getName());
        existing.setCoordinates(updated.getCoordinates());
        existing.setOscarsCount(updated.getOscarsCount());
        existing.setBudget(updated.getBudget());
        existing.setTotalBoxOffice(updated.getTotalBoxOffice());
        existing.setMpaaRating(updated.getMpaaRating());
        existing.setDirector(updated.getDirector());
        existing.setScreenwriter(updated.getScreenwriter());
        existing.setOperator(updated.getOperator());
        existing.setLength(updated.getLength());
        existing.setGoldenPalmCount(updated.getGoldenPalmCount());
        existing.setUsaBoxOffice(updated.getUsaBoxOffice());
        existing.setGenre(updated.getGenre());

        Movie saved = movieRepository.save(existing);
        broadcastAfterCommit("updated", saved.getId());
        return saved;
    }

    public void delete(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Movie not found"));
        movieRepository.delete(movie);
        broadcastAfterCommit("deleted", id);
    }

    public long countByGoldenPalmCount(Long goldenPalmCount) {
        return movieRepository.countByGoldenPalmCount(goldenPalmCount);
    }

    public long countByUsaBoxOfficeGreaterThan(long boxOffice) {
        return movieRepository.countByUsaBoxOfficeGreaterThan(boxOffice);
    }

    public List<Movie> findDistinctByGenreIsNotNull() {
        return movieRepository.findDistinctByGenreIsNotNull();
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
