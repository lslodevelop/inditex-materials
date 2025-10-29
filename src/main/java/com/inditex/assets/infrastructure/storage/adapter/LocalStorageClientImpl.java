package com.inditex.assets.infrastructure.storage.adapter;

import com.inditex.assets.domain.model.Asset;
import com.inditex.assets.domain.port.out.StorageClientPort;
import com.inditex.assets.infrastructure.storage.config.LocalStorageProperties;
import com.inditex.assets.infrastructure.storage.model.StorageMetadata;
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

    private final Path basePath;

    public LocalStorageClientImpl(final LocalStorageProperties props) {
        this.basePath = Path.of(props.basePath());
    }

    @Override
    public Mono<String> getFile(String filename) {
        return Mono.fromCallable(() -> {
            Path filePath = basePath.resolve(filename);
            byte[] fileBytes = Files.readAllBytes(filePath);
            return Base64.getEncoder().encodeToString(fileBytes);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<StorageMetadata> upload(Asset asset, String encodedFile) {
        return Mono.fromCallable(() -> {
                    try {
                        if (!Files.exists(basePath)) {
                            Files.createDirectories(basePath);
                            log.info("Created upload directory at {}", basePath);
                        }

                        byte[] fileBytes = Base64.getDecoder().decode(encodedFile);
                        Path filePath = basePath.resolve(asset.getFilename());

                        Files.write(filePath, fileBytes,
                                StandardOpenOption.CREATE,
                                StandardOpenOption.TRUNCATE_EXISTING);

                        log.info("[LOCAL STORAGE] Saved file '{}' at {}", asset.getFilename(), basePath.toAbsolutePath());

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
