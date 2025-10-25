package com.example.assets.infrastructure.storage.adapter;

import com.example.assets.domain.model.Asset;
import com.example.assets.domain.port.out.StorageClientPort;
import com.example.assets.infrastructure.storage.config.StorageProperties;
import com.example.assets.infrastructure.storage.exception.MinioStorageExceptionHandling;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioStorageClientImpl implements StorageClientPort {

    private final MinioClient minioClient;
    private final StorageProperties storageProperties;
    private final MinioStorageExceptionHandling minioStorageExceptionHandling;

    @Override
    public Mono<Void> publish(final Asset asset, final String encodedFile) {
        return Mono.fromCallable(() -> {
            final String bucket = storageProperties.getBucket();
            byte[] fileBytes = Base64.getDecoder().decode(encodedFile);

            try (InputStream inputStream = new ByteArrayInputStream(fileBytes)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(asset.getFilename())
                        .stream(inputStream, fileBytes.length, -1)
                        .contentType(asset.getContentType())
                        .build()
                );
            }

            log.info("✅ Asset {} uploaded successfully to MinIO", asset.getId());
            return (Void) null;
            })
            .subscribeOn(Schedulers.boundedElastic()) //execute in non-blocking thread
            .onErrorMap(minioStorageExceptionHandling::mapToStorageException);
    }
}
