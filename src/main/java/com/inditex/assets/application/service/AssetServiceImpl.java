package com.inditex.assets.application.service;

import com.inditex.assets.domain.model.Asset;
import com.inditex.assets.domain.model.AssetStatus;
import com.inditex.assets.domain.port.in.UploadAssetPort;
import com.inditex.assets.domain.port.out.AssetRepositoryPort;
import com.inditex.assets.domain.port.out.StorageClientPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements UploadAssetPort {

    private final AssetRepositoryPort assetRepositoryPort;
    private final StorageClientPort storageClientPort;

    @Override
    public Mono<String> upload(final Asset asset, final String encodedFile) {
        final Asset toSave = Asset.builder()
                .id(UUID.randomUUID().toString())
                .filename(asset.getFilename())
                .contentType(asset.getContentType())
                .createdAt(Instant.now())
                .status(AssetStatus.PENDING)
                .build();

        return assetRepositoryPort.save(toSave)
                .flatMap(saved ->
                        storageClientPort.upload(saved, encodedFile)
                                .flatMap(metadata -> assetRepositoryPort.updateMetadataAndStatus(
                                        saved.getId(),
                                        metadata.getUrl(),
                                        metadata.getSize(),
                                        metadata.getUploadedAt(),
                                        null))
                                .onErrorResume(err -> assetRepositoryPort.updateMetadataAndStatus(
                                        saved.getId(),
                                        null,
                                        0L,
                                        null,
                                        err.getMessage()))
                                .thenReturn(saved.getId())
                );
    }

    @Override
    public Flux<Asset> search(final String filename, final String contentType, final String sortBy, final String sortDirection) {
        return assetRepositoryPort.findByFilter(filename, contentType, sortBy, sortDirection)
                .flatMap(asset -> storageClientPort.getFile(asset.getFilename())
                        .map(encoded -> {
                            asset.setEncodedFile(encoded);
                            return asset;
                        })
                        .onErrorResume(e -> {
                            log.warn("File could not be retrieved {}: {}", asset.getFilename(), e.getMessage());
                            return Mono.just(asset);
                        })
                );
        }
}
