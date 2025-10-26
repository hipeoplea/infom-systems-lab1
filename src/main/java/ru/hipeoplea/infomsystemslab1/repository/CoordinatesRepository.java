package ru.hipeoplea.infomsystemslab1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.hipeoplea.infomsystemslab1.models.Coordinates;

@Repository
public interface CoordinatesRepository extends JpaRepository<Coordinates, Long> {
}

