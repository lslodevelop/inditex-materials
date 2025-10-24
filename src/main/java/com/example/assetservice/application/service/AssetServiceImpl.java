package com.example.assetservice.application.service;

import com.example.assetservice.domain.model.Asset;
import com.example.assetservice.domain.model.AssetStatus;
import com.example.assetservice.domain.port.in.UploadAssetUseCase;
import com.example.assetservice.domain.port.out.AssetRepositoryPort;
import com.example.assetservice.domain.port.out.PublisherPort;
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
        Asset toSave = Asset.builder()
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
                        .flatMap(v -> repository.updateStatus(saved.getId(), saved.getUrl(), saved.getSize(), null))
                        .onErrorResume(err -> repository.updateStatus(saved.getId(), null, null, err.getMessage()))
                        .subscribe();

                    return Mono.just(saved.getId());
                });
    }

    @Override
    public Flux<Asset> search(String filename, String contentType) {
        return repository.findByFilter(filename, contentType);
    }
}
