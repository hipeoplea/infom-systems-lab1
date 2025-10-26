package ru.hipeoplea.infomsystemslab1.repository;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.hipeoplea.infomsystemslab1.models.Movie;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    long countByGoldenPalmCount(@Positive @NotNull Long goldenPalmCount);

    long countByUsaBoxOfficeGreaterThan(@Positive long usaBoxOffice);

    List<Movie> findDistinctByGenreIsNotNull();

    @Query("select m from Movie m " +
            "left join m.director d " +
            "left join m.screenwriter s " +
            "left join m.operator o " +
            "where lower(m.name) like lower(concat('%', :q, '%')) " +
            "   or lower(d.name) like lower(concat('%', :q, '%')) " +
            "   or lower(s.name) like lower(concat('%', :q, '%')) " +
            "   or lower(o.name) like lower(concat('%', :q, '%'))")
    Page<Movie> search(@Param("q") String q, Pageable pageable);
}
