package com.example.assets.infrastructure.storage.adapter;

import com.example.assets.domain.model.Asset;
import com.example.assets.domain.port.out.StorageClientPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;

@Slf4j
@Component
@Profile("local")
public class LocalStorageClientImpl implements StorageClientPort {

    private static final Path BASE_PATH = Path.of("/tmp/uploads");

    @Override
    public Mono<Void> upload(Asset asset, String encodedFile) {
        return Mono.fromCallable(() -> {
                    try {
                        if (!Files.exists(BASE_PATH)) {
                            Files.createDirectories(BASE_PATH);
                            log.info("Created upload directory at {}", BASE_PATH);
                        }

                        byte[] decoded = Base64.getDecoder().decode(encodedFile);
                        Path filePath = BASE_PATH.resolve(asset.getFilename());

                        Files.write(filePath, decoded,
                                StandardOpenOption.CREATE,
                                StandardOpenOption.TRUNCATE_EXISTING);

                        log.info("[LOCAL STORAGE] Saved file '{}' at {}", asset.getFilename(), BASE_PATH.toAbsolutePath());
                        return true;
                    } catch (IOException e) {
                        throw new RuntimeException("Error writing local file", e);
                    } catch (IllegalArgumentException e) {
                        throw new RuntimeException("Invalid Base64 input for asset " + asset.getFilename(), e);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

}
