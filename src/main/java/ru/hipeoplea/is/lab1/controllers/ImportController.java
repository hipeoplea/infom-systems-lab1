package ru.hipeoplea.is.lab1.controllers;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.hipeoplea.is.lab1.generated.api.ImportsApi;
import ru.hipeoplea.is.lab1.models.ImportOperation;
import ru.hipeoplea.is.lab1.repository.ImportOperationRepository;

@RestController
@RequiredArgsConstructor
public class ImportController implements ImportsApi {
    private final ImportOperationRepository importOperationRepository;

    @Override
    public ResponseEntity<List<ImportOperation>> listImportsForUser(
            String user) {
        return ResponseEntity.ok(
                importOperationRepository.findByUserOrderByCreatedAtDesc(
                        user));
    }

    @Override
    public ResponseEntity<List<ImportOperation>> listAllImports() {
        return ResponseEntity.ok(
                importOperationRepository.findAllByOrderByCreatedAtDesc());
    }
}
