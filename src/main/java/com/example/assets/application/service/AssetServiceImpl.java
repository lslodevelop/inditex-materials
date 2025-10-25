package com.example.assets.application.service;

import com.example.assets.domain.model.Asset;
import com.example.assets.domain.model.AssetStatus;
import com.example.assets.domain.port.in.UploadAssetUseCase;
import com.example.assets.domain.port.out.AssetRepositoryPort;
import com.example.assets.domain.port.out.PublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements UploadAssetUseCase {

    private final AssetRepositoryPort repository;
    private final PublisherPort publisher;

    @Override
    public Mono<String> upload(final Asset asset, final String encodedFile) {
        final Asset toSave = Asset.builder()
                .id(UUID.randomUUID().toString())
                .filename(asset.getFilename())
                .contentType(asset.getContentType())
                .createdAt(Instant.now())
                .status(AssetStatus.PENDING)
                .build();

        return repository.save(toSave)
                .flatMap(saved -> {
                    // Ejecutar publish en background (mockable)
                    publisher.publish(saved, encodedFile)
                        .flatMap(v -> repository.updateStatus(saved.getId(), null))
                        .onErrorResume(err -> repository.updateStatus(saved.getId(), err.getMessage()))
                        .subscribe();

                    return Mono.just(saved.getId());
                });
    }

    @Override
    public Flux<Asset> search(final String filename, final String contentType) {
        return repository.findByFilter(filename, contentType);
    }
}
