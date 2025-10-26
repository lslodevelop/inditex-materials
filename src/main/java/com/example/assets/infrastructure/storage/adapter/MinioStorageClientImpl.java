package com.example.assets.infrastructure.storage.adapter;

import com.example.assets.domain.model.Asset;
import com.example.assets.domain.port.out.StorageClientPort;
import com.example.assets.infrastructure.storage.config.StorageProperties;
import com.example.assets.infrastructure.storage.exception.MinioStorageExceptionHandling;
import com.example.assets.infrastructure.storage.model.StorageMetadata;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("minio")
public class MinioStorageClientImpl implements StorageClientPort {

    private final MinioClient minioClient;
    private final StorageProperties storageProperties;
    private final MinioStorageExceptionHandling minioStorageExceptionHandling;

    @Override
    public Mono<String> getFile(final String filename) {
        return Mono.fromCallable(() -> {
            try (InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(storageProperties.getBucket())
                            .object(filename)
                            .build())) {
                return Base64.getEncoder().encodeToString(stream.readAllBytes());
            }
        }).subscribeOn(Schedulers.boundedElastic()); // Execute blocking tasks (IO) W/O blocking main reactive thread
    }

    @Override
    public Mono<StorageMetadata> upload(final Asset asset, final String encodedFile) {
        return Mono.fromCallable(() -> {
            final String bucket = storageProperties.getBucket();
            final byte[] fileBytes = Base64.getDecoder().decode(encodedFile);
            final long size = fileBytes.length;

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

            final String url = String.format("%s/%s/%s", storageProperties.getEndpoint(),
                    storageProperties.getBucket(), asset.getFilename()
            );

            log.info("Asset {} uploaded successfully to MinIO", asset.getId());
            return StorageMetadata.builder()
                    .url(url)
                    .size(size)
                    .uploadedAt(Instant.now())
                    .build();
            })
            .subscribeOn(Schedulers.boundedElastic()) //execute in non-blocking thread
            .onErrorMap(minioStorageExceptionHandling::mapToStorageException);
    }
}
