package ru.hipeoplea.is.lab1;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.hipeoplea.is.lab1.models.ImportStatus;
import ru.hipeoplea.is.lab1.repository.ImportOperationRepository;
import ru.hipeoplea.is.lab1.services.FileStorageService;
import ru.hipeoplea.is.lab1.services.ImportService;
import ru.hipeoplea.is.lab1.repository.MovieRepository;
import io.minio.MinioClient;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "classpath:schema.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ImportServiceRollbackTest {

    @Autowired
    private ImportService importService;

    @Autowired
    private ImportOperationRepository importOperationRepository;


    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private MovieRepository movieRepository;

    @MockBean
    private MinioClient minioClient;

    @Test
    void shouldRollbackAndCleanupTempOnRuntimeException() throws Exception {
        String tempKey = "imports/1/temp/mock.json";
        when(fileStorageService.uploadImportFile(any(), any(), any()))
                .thenReturn(tempKey);
        doThrow(new RuntimeException("boom")).when(movieRepository)
                .save(any());

        MockMultipartFile file = new MockMultipartFile(
                "file", "movies.json", "application/json",
                minimalValidPayload().getBytes(StandardCharsets.UTF_8));

        assertThrows(RuntimeException.class,
                () -> importService.importMovies(file, "tester"));

        importOperationRepository.findAll()
                .forEach(op -> {
                    assert op.getStatus() == ImportStatus.FAILED
                            || op.getStatus() == ImportStatus.IN_PROGRESS;
                });
    }

    private String minimalValidPayload() {
        return """
                [
                  {
                    "name": "Test",
                    "coordinates": { "x": 1.0, "y": 2.0 },
                    "creationDate": "2024-01-01T00:00:00Z",
                    "oscarsCount": 1,
                    "budget": 1000,
                    "totalBoxOffice": 2000,
                    "mpaaRating": "PG",
                    "director": {
                      "name": "Dir",
                      "eyeColor": "BLUE",
                      "nationality": "USA",
                      "passportID": "PASS1234567",
                      "location": { "x": 1, "y": 1, "name": "Loc" }
                    },
                    "operator": {
                      "name": "Op",
                      "eyeColor": "BLACK",
                      "nationality": "GERMANY",
                      "passportID": "PASS7654321",
                      "location": { "x": 2, "y": 2, "name": "Loc2" }
                    },
                    "length": 90,
                    "goldenPalmCount": 1,
                    "usaBoxOffice": 1500,
                    "genre": "ADVENTURE"
                  }
                ]
                """;
    }
}
