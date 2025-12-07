package ru.hipeoplea.is.lab1.controllers;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.hipeoplea.is.lab1.generated.api.ImportsApi;
import ru.hipeoplea.is.lab1.models.ImportOperation;
import ru.hipeoplea.is.lab1.repository.ImportOperationRepository;
import ru.hipeoplea.is.lab1.services.FileStorageService;
import ru.hipeoplea.is.lab1.services.ImportService;
import org.springframework.core.io.Resource;
import ru.hipeoplea.is.lab1.exeption.NotFoundException;

@RestController
@RequiredArgsConstructor
public class ImportController implements ImportsApi {
    private final ImportOperationRepository importOperationRepository;
    private final FileStorageService fileStorageService;
    private final ImportService importService;

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

    @GetMapping("/api/imports/{id}/file")
    public ResponseEntity<Resource> downloadImportFile(
            @PathVariable Long id) {
        ImportOperation op = importOperationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Import not found"));
        if (op.getFileKey() == null) {
            throw new NotFoundException("Файл импорта не найден");
        }
        Resource file = fileStorageService.download(op.getFileKey());
        String fileName = op.getFileName() == null
                ? ("import-" + id + ".json") : op.getFileName();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);
    }

    @PostMapping("/api/imports/{id}/retry-finalize")
    public ResponseEntity<Void> retryFinalize(@PathVariable Long id) {
        importService.retryFinalizeFile(id);
        return ResponseEntity.accepted().build();
    }
}
