package com.example.assets.infrastructure.storage.adapter;

import com.example.assets.domain.model.Asset;
import com.example.assets.domain.port.out.StorageClientPort;
import com.example.assets.infrastructure.storage.model.StorageMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Base64;

@Slf4j
@Component
@Profile("local")
public class LocalStorageClientImpl implements StorageClientPort {

    private static final Path BASE_PATH = Path.of("/tmp/uploads");

    @Override
    public Mono<String> getFile(String filename) {
        return Mono.fromCallable(() -> {
            Path filePath = BASE_PATH.resolve(filename);
            byte[] fileBytes = Files.readAllBytes(filePath);
            return Base64.getEncoder().encodeToString(fileBytes);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<StorageMetadata> upload(Asset asset, String encodedFile) {
        return Mono.fromCallable(() -> {
                    try {
                        if (!Files.exists(BASE_PATH)) {
                            Files.createDirectories(BASE_PATH);
                            log.info("Created upload directory at {}", BASE_PATH);
                        }

                        byte[] fileBytes = Base64.getDecoder().decode(encodedFile);
                        Path filePath = BASE_PATH.resolve(asset.getFilename());

                        Files.write(filePath, fileBytes,
                                StandardOpenOption.CREATE,
                                StandardOpenOption.TRUNCATE_EXISTING);

                        log.info("[LOCAL STORAGE] Saved file '{}' at {}", asset.getFilename(), BASE_PATH.toAbsolutePath());

                        return StorageMetadata.builder()
                                .url(filePath.toString())
                                .size(fileBytes.length)
                                .uploadedAt(Instant.now())
                                .build();

                    } catch (IOException e) {
                        throw new RuntimeException("Error writing local file", e);
                    } catch (IllegalArgumentException e) {
                        throw new RuntimeException("Invalid Base64 input for asset " + asset.getFilename(), e);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

}
