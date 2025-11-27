package ru.hipeoplea.is.lab1.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.hipeoplea.is.lab1.models.ImportOperation;
import ru.hipeoplea.is.lab1.models.ImportStatus;
import ru.hipeoplea.is.lab1.repository.ImportOperationRepository;

/**
 * Marks stale IN_PROGRESS import operations as FAILED.
 */
@Component
@RequiredArgsConstructor
public class ImportRecoveryJob {
    private static final int STALE_MINUTES = 10;
    private static final long DELAY = 300000L;
    private final ImportOperationRepository importOperationRepository;

    @Scheduled(fixedDelay = DELAY)
    @Transactional
    public void recoverStaleImports() {
        Instant threshold = Instant.now().minus(
                STALE_MINUTES, ChronoUnit.MINUTES);
        List<ImportOperation> stale =
                importOperationRepository.findByStatusAndCreatedAtBefore(
                        ImportStatus.IN_PROGRESS, threshold);

        for (ImportOperation op : stale) {
            op.setStatus(ImportStatus.FAILED);
        }
        if (!stale.isEmpty()) {
            importOperationRepository.saveAll(stale);
        }
    }
}
