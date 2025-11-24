package ru.hipeoplea.is.lab1.repository;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.hipeoplea.is.lab1.models.Movie;
import ru.hipeoplea.is.lab1.models.MovieGenre;
import ru.hipeoplea.is.lab1.models.MpaaRating;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    long countByGoldenPalmCount(@Positive @NotNull Long goldenPalmCount);

    long countByUsaBoxOfficeGreaterThan(@Positive long usaBoxOffice);

    List<Movie> findDistinctByGenreIsNotNull();

    @Query(
            "select m from Movie m "
                    + "left join m.director d "
                    + "left join m.screenwriter s "
                    + "left join m.operator o "
                    + "where lower(m.name) like lower(concat('%', :q, '%')) "
                    + "or lower(d.name) like lower(concat('%', :q, '%')) "
                    + "or lower(s.name) like lower(concat('%', :q, '%')) "
                    + "or lower(o.name) like lower(concat('%', :q, '%'))")
    Page<Movie> search(@Param("q") String q, Pageable pageable);

    @Query(
            "select distinct m.director.id from Movie m "
                    + "where m.genre = :genre and m.director is not null")
    List<Long> findDirectorIdsByGenre(@Param("genre") MovieGenre genre);

    @Modifying
    @Query(
            "update Movie m set m.oscarsCount = 0 "
                    + "where m.director.id in :directorIds")
    void resetOscarsByDirectorIds(@Param("directorIds") List<Long> directorIds);

    @Modifying
    @Query(
            "update Movie m set m.oscarsCount = coalesce(m.oscarsCount, 0) + 1 "
                    + "where m.mpaaRating = :rating")
    void incrementOscarsByRating(@Param("rating") MpaaRating rating);
}
