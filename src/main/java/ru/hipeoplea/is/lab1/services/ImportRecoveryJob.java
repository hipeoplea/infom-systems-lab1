package ru.hipeoplea.is.lab1.services;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
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
    @Value("${jobs.import-recovery.stale-duration}")
    private Duration staleDuration;
    private final Clock clock = Clock.systemUTC();
    private final ImportOperationRepository importOperationRepository;

    @SchedulerLock(name = "importRecoveryJob")
    @Scheduled(fixedDelayString = "${jobs.import-recovery.delay-ms}")
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void recoverStaleImports() {
        OffsetDateTime threshold = OffsetDateTime.ofInstant(
                clock.instant().minus(staleDuration), ZoneOffset.UTC);
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
