package ru.hipeoplea.is.lab1.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.hipeoplea.is.lab1.models.ImportOperation;
import ru.hipeoplea.is.lab1.models.ImportStatus;

@Repository
public interface ImportOperationRepository
        extends JpaRepository<ImportOperation, Long> {
    List<ImportOperation> findByUserOrderByCreatedAtDesc(String user);

    List<ImportOperation> findAllByOrderByCreatedAtDesc();

    List<ImportOperation> findByStatusAndCreatedAtBefore(
            ImportStatus status, java.time.Instant threshold);
}
