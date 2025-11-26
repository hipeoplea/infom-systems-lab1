package ru.hipeoplea.is.lab1.services;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hipeoplea.is.lab1.models.Movie;
import ru.hipeoplea.is.lab1.models.MovieGenre;
import ru.hipeoplea.is.lab1.models.MpaaRating;
import ru.hipeoplea.is.lab1.repository.MovieRepository;

@Service
@Transactional
public class OpsService {
    private final MovieRepository movieRepository;

    public OpsService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    /**
     * Counts movies by Golden Palm count.
     */
    @Transactional(readOnly = true)
    public long countGoldenPalm(Long value) {
        return movieRepository.countByGoldenPalmCount(value);
    }

    /**
     * Counts movies with US box office above the provided value.
     */
    @Transactional(readOnly = true)
    public long countUsaBoxOfficeGreater(long value) {
        return movieRepository.countByUsaBoxOfficeGreaterThan(value);
    }

    /**
     * Returns sorted unique genre names.
     */
    @Transactional(readOnly = true)
    public List<String> uniqueGenres() {
        return movieRepository.findDistinctByGenreIsNotNull().stream()
                .map(Movie::getGenre)
                .map(Enum::name)
                .distinct()
                .sorted()
                .toList();
    }

    /**
     * Increments Oscars for movies with MPAA rating R.
     */
    public void addOscarToR() {
        movieRepository.incrementOscarsByRating(MpaaRating.R);
    }

    /**
     * Resets Oscars for directors whose movies match the provided genre.
     */
    public void removeOscarsByDirectorsGenre(MovieGenre genre) {
        List<Long> directorIds = movieRepository.findDirectorIdsByGenre(genre);
        if (directorIds == null || directorIds.isEmpty()) {
            return;
        }
        movieRepository.resetOscarsByDirectorIds(directorIds);
    }
}
