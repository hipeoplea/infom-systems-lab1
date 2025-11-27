package ru.hipeoplea.is.lab1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.hipeoplea.is.lab1.models.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    boolean existsByNameAndXAndY(String name, Float x, int y);

    boolean existsByNameAndXAndYAndIdNot(String name, Float x, int y, Long id);
}
