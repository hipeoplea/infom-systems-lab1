package ru.hipeoplea.is.lab1;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.hipeoplea.is.lab1.exeption.NotFoundException;
import ru.hipeoplea.is.lab1.models.Color;
import ru.hipeoplea.is.lab1.models.Coordinates;
import ru.hipeoplea.is.lab1.models.Movie;
import ru.hipeoplea.is.lab1.models.MovieGenre;
import ru.hipeoplea.is.lab1.models.MpaaRating;
import ru.hipeoplea.is.lab1.models.Person;
import ru.hipeoplea.is.lab1.repository.MovieRepository;
import ru.hipeoplea.is.lab1.services.MovieService;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {
    private static final long SAVED_ID = 100L;
    private static final long LOOKUP_ID = 5L;
    private static final long EXISTING_ID = 10L;
    private static final long NEW_GOLDEN_PALM = 77L;
    private static final long NEW_USA_BOX_OFFICE = 9_999L;
    private static final long NOT_FOUND_ID = 404L;
    private static final long DELETE_ID = 55L;
    private static final long COUNT_GOLDEN = 5L;
    private static final long COUNT_GOLDEN_RESULT = 7L;
    private static final long BOX_OFFICE_THRESHOLD = 1_000L;
    private static final long BOX_OFFICE_RESULT = 3L;
    private static final long SEARCH_RESULT_ID = 3L;
    private static final int PAGE_SIZE_TEN = 10;
    private static final int PAGE_SIZE_FIVE = 5;
    private static final int PAGE_SIZE_TWO = 2;
    private static final long COORD_ID = 10L;
    private static final float COORD_X = 1.0f;
    private static final double COORD_Y = 2.0;
    private static final long DIRECTOR_ID = 11L;
    private static final long SCREENWRITER_ID = 12L;
    private static final long OPERATOR_ID = 13L;
    private static final long OSCARS_COUNT = 1L;
    private static final int BUDGET = 100;
    private static final int TOTAL_BOX_OFFICE = 200;
    private static final int LENGTH = 120;
    private static final long GOLDEN_PALM = 5L;
    private static final long USA_BOX_OFFICE = 300L;

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieService service;

    @Test
    void createSaves() {
        Movie toSave = sampleMovie(null);
        Movie saved = sampleMovie(SAVED_ID);
        when(movieRepository.save(any(Movie.class))).thenReturn(saved);

        Movie actual = service.create(toSave);

        assertEquals(saved, actual);
        verify(movieRepository).save(toSave);
    }

    @Test
    void getByIdDelegatesToRepository() {
        Movie m = sampleMovie(LOOKUP_ID);
        when(movieRepository.findById(LOOKUP_ID)).thenReturn(Optional.of(m));

        Optional<Movie> res = service.getById(LOOKUP_ID);
        assertTrue(res.isPresent());
        assertEquals(m, res.get());
    }

    @Test
    void getAllDelegatesToRepository() {
        Pageable pageable = PageRequest.of(0, PAGE_SIZE_TEN);
        Page<Movie> page = new PageImpl<>(
                List.of(sampleMovie(1L), sampleMovie(2L)),
                pageable,
                PAGE_SIZE_TWO);
        when(movieRepository.findAll(pageable)).thenReturn(page);

        Page<Movie> result = service.getAll(pageable);
        assertEquals(2, result.getTotalElements());
        verify(movieRepository).findAll(pageable);
    }

    @Test
    void searchDelegatesToRepository() {
        Pageable pageable = PageRequest.of(0, PAGE_SIZE_FIVE);
        Page<Movie> page =
                new PageImpl<>(
                        List.of(sampleMovie(SEARCH_RESULT_ID)),
                        pageable,
                        1);
        when(movieRepository.search("neo", pageable)).thenReturn(page);

        Page<Movie> result = service.search("neo", pageable);
        assertEquals(1, result.getTotalElements());
        verify(movieRepository).search("neo", pageable);
    }

    @Test
    void updateUpdatesFields() {
        Movie existing = sampleMovie(EXISTING_ID);
        Movie incoming = sampleMovie(null);
        incoming.setName("Updated");
        incoming.setGoldenPalmCount(NEW_GOLDEN_PALM);
        incoming.setUsaBoxOffice(NEW_USA_BOX_OFFICE);

        when(movieRepository.findById(EXISTING_ID))
                .thenReturn(Optional.of(existing));
        when(movieRepository.save(any(Movie.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Movie updated = service.update(EXISTING_ID, incoming);

        assertEquals("Updated", updated.getName());
        assertEquals(NEW_GOLDEN_PALM, updated.getGoldenPalmCount());
        assertEquals(NEW_USA_BOX_OFFICE, updated.getUsaBoxOffice());
        verify(movieRepository).save(existing);
    }

    @Test
    void updateThrowsWhenNotFound() {
        when(movieRepository.findById(NOT_FOUND_ID))
                .thenReturn(Optional.empty());
        assertThrows(
                NotFoundException.class,
                () -> service.update(NOT_FOUND_ID, sampleMovie(null)));
        verify(movieRepository, never()).save(any());
    }

    @Test
    void deleteDeletes() {
        Movie existing = sampleMovie(DELETE_ID);
        when(movieRepository.findById(DELETE_ID))
                .thenReturn(Optional.of(existing));

        service.delete(DELETE_ID);

        verify(movieRepository).delete(existing);
    }

    @Test
    void deleteThrowsWhenNotFound() {
        when(movieRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.delete(1L));
        verify(movieRepository, never()).delete(any());
    }

    @Test
    void countsAndDistinctDelegatesToRepository() {
        when(movieRepository.countByGoldenPalmCount(COUNT_GOLDEN))
                .thenReturn(COUNT_GOLDEN_RESULT);
        when(movieRepository.countByUsaBoxOfficeGreaterThan(
                        BOX_OFFICE_THRESHOLD))
                .thenReturn(BOX_OFFICE_RESULT);
        when(movieRepository.findDistinctByGenreIsNotNull())
                .thenReturn(List.of(sampleMovie(1L)));

        assertEquals(
                COUNT_GOLDEN_RESULT,
                service.countByGoldenPalmCount(COUNT_GOLDEN));
        assertEquals(
                BOX_OFFICE_RESULT,
                service.countByUsaBoxOfficeGreaterThan(BOX_OFFICE_THRESHOLD));
        assertEquals(1, service.findDistinctByGenreIsNotNull().size());
    }

    private Movie sampleMovie(Long id) {
        Coordinates coords = new Coordinates();
        coords.setId(COORD_ID);
        coords.setX(COORD_X);
        coords.setY(COORD_Y);

        Person director = new Person();
        director.setId(DIRECTOR_ID);
        director.setName("Dir");
        director.setEyeColor(Color.BLUE);

        Person screenwriter = new Person();
        screenwriter.setId(SCREENWRITER_ID);
        screenwriter.setName("Scr");
        screenwriter.setEyeColor(Color.BLUE);

        Person operator = new Person();
        operator.setId(OPERATOR_ID);
        operator.setName("Op");
        operator.setEyeColor(Color.BLUE);

        return Movie.builder()
                .id(id)
                .name("Test")
                .coordinates(coords)
                .creationDate(new Date())
                .oscarsCount(OSCARS_COUNT)
                .budget(BUDGET)
                .totalBoxOffice(TOTAL_BOX_OFFICE)
                .mpaaRating(MpaaRating.PG)
                .director(director)
                .screenwriter(screenwriter)
                .operator(operator)
                .length(LENGTH)
                .goldenPalmCount(GOLDEN_PALM)
                .usaBoxOffice(USA_BOX_OFFICE)
                .genre(MovieGenre.ADVENTURE)
                .build();
    }
}
