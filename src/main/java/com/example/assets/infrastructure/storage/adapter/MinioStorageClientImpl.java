package com.example.assets.infrastructure.storage.adapter;

import com.example.assets.domain.model.Asset;
import com.example.assets.domain.port.out.PublisherPort;
import com.example.assets.infrastructure.storage.config.StorageProperties;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioStorageClientImpl implements PublisherPort {

    private final MinioClient minioClient;
    private final StorageProperties storageProperties;

    @Override
    public Mono<Void> publish(final Asset asset, final String encodedFile) {
        return Mono.fromRunnable(() -> {
            try {
                String bucket = storageProperties.getBucket();

                // Decodificar el archivo base64
                byte[] fileBytes = Base64.getDecoder().decode(encodedFile);

                // Subir a MinIO
                try (final InputStream inputStream = new ByteArrayInputStream(fileBytes)) {
                    minioClient.putObject(
                            PutObjectArgs.builder()
                                    .bucket(bucket)
                                    .object(asset.getFilename())
                                    .stream(inputStream, fileBytes.length, -1)
                                    .contentType(asset.getContentType())
                                    .build()
                    );
                }

                log.info("✅ Asset {} subido correctamente a MinIO", asset.getId());

            } catch (Exception e) {
                log.error("❌ Error subiendo asset {} a MinIO", asset.getId(), e);
                throw new RuntimeException(e);
            }
        });
    }
}
