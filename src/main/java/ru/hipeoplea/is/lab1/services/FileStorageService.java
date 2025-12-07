package ru.hipeoplea.is.lab1.services;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.hipeoplea.is.lab1.config.MinioProperties;

@Service
@RequiredArgsConstructor
public class FileStorageService {
    private static final Logger log =
            LoggerFactory.getLogger(FileStorageService.class);
    private final MinioClient minioClient;
    private final MinioProperties props;

    public String uploadImportFile(String objectName, byte[] data,
            String contentType) {
        try (InputStream in = new ByteArrayInputStream(data)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(props.getBucket())
                            .object(objectName)
                            .stream(in, data.length, -1)
                            .contentType(contentType)
                            .build());
            log.info("Stored import file {}", objectName);
            return objectName;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to store import file in MinIO", e);
        }
    }

    public Resource download(String objectName) {
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(props.getBucket())
                            .object(objectName)
                            .build());
            return new InputStreamResource(stream);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to download import file " + objectName, e);
        }
    }

    public void deleteQuietly(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(props.getBucket())
                            .object(objectName)
                    .build());
        } catch (Exception e) {
            log.warn("Cannot delete MinIO object {}: {}", objectName,
                    e.getMessage());
        }
    }

    public void copyObject(String sourceObject, String targetObject) {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(props.getBucket())
                            .object(targetObject)
                            .source(CopySource.builder()
                                    .bucket(props.getBucket())
                                    .object(sourceObject)
                                    .build())
                            .build());
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to copy MinIO object from " + sourceObject
                            + " to " + targetObject, e);
        }
    }
}
