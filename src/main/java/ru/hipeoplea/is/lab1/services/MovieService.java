package ru.hipeoplea.is.lab1.services;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hipeoplea.is.lab1.exeption.NotFoundException;
import ru.hipeoplea.is.lab1.models.Movie;
import ru.hipeoplea.is.lab1.repository.MovieRepository;

@Service
@Transactional
public class MovieService {
    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    /**
     * Creates a movie.
     */
    public Movie create(Movie movie) {
        return movieRepository.save(movie);
    }

    /**
     * Looks up a movie by id.
     */
    @Transactional(readOnly = true)
    public Optional<Movie> getById(Long id) {
        return movieRepository.findById(id);
    }

    /**
     * Returns a page of movies.
     */
    @Transactional(readOnly = true)
    public Page<Movie> getAll(Pageable pageable) {
        return movieRepository.findAll(pageable);
    }

    /**
     * Searches movies by a query string.
     */
    @Transactional(readOnly = true)
    public Page<Movie> search(String q, Pageable pageable) {
        return movieRepository.search(q, pageable);
    }

    /**
     * Updates an existing movie.
     */
    @Transactional
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

        return movieRepository.save(existing);
    }

    /**
     * Deletes a movie by id.
     */
    public void delete(Long id) {
        Movie movie =
                movieRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Movie %d not found"
                                                        .formatted(id)));
        movieRepository.delete(movie);
    }

    /**
     * Counts movies by Golden Palm count.
     */
    public long countByGoldenPalmCount(Long goldenPalmCount) {
        return movieRepository.countByGoldenPalmCount(goldenPalmCount);
    }

    /**
     * Counts movies whose US box office is greater than given value.
     */
    public long countByUsaBoxOfficeGreaterThan(long boxOffice) {
        return movieRepository.countByUsaBoxOfficeGreaterThan(boxOffice);
    }

    /**
     * Returns movies that have genre set.
     */
    public List<Movie> findDistinctByGenreIsNotNull() {
        return movieRepository.findDistinctByGenreIsNotNull();
    }
}
