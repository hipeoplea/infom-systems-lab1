package ru.hipeoplea.infomsystemslab1;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.hipeoplea.infomsystemslab1.exeption.NotFoundException;
import ru.hipeoplea.infomsystemslab1.models.Coordinates;
import ru.hipeoplea.infomsystemslab1.models.Movie;
import ru.hipeoplea.infomsystemslab1.models.MovieGenre;
import ru.hipeoplea.infomsystemslab1.models.MpaaRating;
import ru.hipeoplea.infomsystemslab1.models.Person;
import ru.hipeoplea.infomsystemslab1.repository.MovieRepository;
import ru.hipeoplea.infomsystemslab1.services.MovieService;
import ru.hipeoplea.infomsystemslab1.websocket.WsHub;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;
    @Mock
    private WsHub wsHub;

    @InjectMocks
    private MovieService service;

    @Test
    void create_savesAndBroadcasts() {
        Movie toSave = sampleMovie(null);
        Movie saved = sampleMovie(100L);
        when(movieRepository.save(any(Movie.class))).thenReturn(saved);

        Movie actual = service.create(toSave);

        assertEquals(saved, actual);
        verify(movieRepository).save(toSave);
        ArgumentCaptor<String> msg = ArgumentCaptor.forClass(String.class);
        verify(wsHub).broadcast(msg.capture());
        String payload = msg.getValue();
        assertTrue(payload.contains("\"type\":\"created\""));
        assertTrue(payload.contains("\"id\":100"));
    }

    @Test
    void getById_delegatesToRepository() {
        Movie m = sampleMovie(5L);
        when(movieRepository.findById(5L)).thenReturn(Optional.of(m));

        Optional<Movie> res = service.getById(5L);
        assertTrue(res.isPresent());
        assertEquals(m, res.get());
    }

    @Test
    void getAll_delegatesToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Movie> page = new PageImpl<>(List.of(sampleMovie(1L), sampleMovie(2L)), pageable, 2);
        when(movieRepository.findAll(pageable)).thenReturn(page);

        Page<Movie> result = service.getAll(pageable);
        assertEquals(2, result.getTotalElements());
        verify(movieRepository).findAll(pageable);
    }

    @Test
    void search_delegatesToRepository() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Movie> page = new PageImpl<>(List.of(sampleMovie(3L)), pageable, 1);
        when(movieRepository.search("neo", pageable)).thenReturn(page);

        Page<Movie> result = service.search("neo", pageable);
        assertEquals(1, result.getTotalElements());
        verify(movieRepository).search("neo", pageable);
    }

    @Test
    void update_updatesFieldsAndBroadcasts() {
        Movie existing = sampleMovie(10L);
        Movie incoming = sampleMovie(null);
        incoming.setName("Updated");
        incoming.setGoldenPalmCount(77L);
        incoming.setUsaBoxOffice(9999);

        when(movieRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(movieRepository.save(any(Movie.class))).thenAnswer(inv -> inv.getArgument(0));

        Movie updated = service.update(10L, incoming);

        assertEquals("Updated", updated.getName());
        assertEquals(77L, updated.getGoldenPalmCount());
        assertEquals(9999L, updated.getUsaBoxOffice());
        verify(movieRepository).save(existing);
        ArgumentCaptor<String> msg = ArgumentCaptor.forClass(String.class);
        verify(wsHub).broadcast(msg.capture());
        assertTrue(msg.getValue().contains("\"type\":\"updated\""));
        assertTrue(msg.getValue().contains("\"id\":10"));
    }

    @Test
    void update_throwsWhenNotFound() {
        when(movieRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.update(404L, sampleMovie(null)));
        verify(movieRepository, never()).save(any());
        verifyNoInteractions(wsHub);
    }

    @Test
    void delete_deletesAndBroadcasts() {
        Movie existing = sampleMovie(55L);
        when(movieRepository.findById(55L)).thenReturn(Optional.of(existing));

        service.delete(55L);

        verify(movieRepository).delete(existing);
        ArgumentCaptor<String> msg = ArgumentCaptor.forClass(String.class);
        verify(wsHub).broadcast(msg.capture());
        assertTrue(msg.getValue().contains("\"type\":\"deleted\""));
        assertTrue(msg.getValue().contains("\"id\":55"));
    }

    @Test
    void delete_throwsWhenNotFound() {
        when(movieRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.delete(1L));
        verify(movieRepository, never()).delete(any());
        verifyNoInteractions(wsHub);
    }

    @Test
    void countsAndDistinctDelegatesToRepository() {
        when(movieRepository.countByGoldenPalmCount(5L)).thenReturn(7L);
        when(movieRepository.countByUsaBoxOfficeGreaterThan(1000L)).thenReturn(3L);
        when(movieRepository.findDistinctByGenreIsNotNull()).thenReturn(List.of(sampleMovie(1L)));

        assertEquals(7L, service.countByGoldenPalmCount(5L));
        assertEquals(3L, service.countByUsaBoxOfficeGreaterThan(1000L));
        assertEquals(1, service.findDistinctByGenreIsNotNull().size());
    }

    private Movie sampleMovie(Long id) {
        Coordinates coords = new Coordinates();
        coords.setId(10L);
        coords.setX(1.0f);
        coords.setY(2.0);

        Person director = new Person();
        director.setId(11L);
        director.setName("Dir");
        director.setEyeColor(ru.hipeoplea.infomsystemslab1.models.Color.BLUE);

        Person screenwriter = new Person();
        screenwriter.setId(12L);
        screenwriter.setName("Scr");
        screenwriter.setEyeColor(ru.hipeoplea.infomsystemslab1.models.Color.BLUE);

        Person operator = new Person();
        operator.setId(13L);
        operator.setName("Op");
        operator.setEyeColor(ru.hipeoplea.infomsystemslab1.models.Color.BLUE);

        return Movie.builder()
                .id(id)
                .name("Test")
                .coordinates(coords)
                .creationDate(new Date())
                .oscarsCount(1L)
                .budget(100)
                .totalBoxOffice(200)
                .mpaaRating(MpaaRating.PG)
                .director(director)
                .screenwriter(screenwriter)
                .operator(operator)
                .length(120)
                .goldenPalmCount(5L)
                .usaBoxOffice(300)
                .genre(MovieGenre.ADVENTURE)
                .build();
    }
}

