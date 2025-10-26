package com.example.assets.infrastructure.database.adapter;

import com.example.assets.domain.model.Asset;
import com.example.assets.domain.model.AssetStatus;
import com.example.assets.domain.port.out.AssetRepositoryPort;
import com.example.assets.infrastructure.database.mapper.AssetEntityMapper;
import com.example.assets.infrastructure.database.entity.AssetEntity;
import com.example.assets.infrastructure.database.repository.AssetR2dbcRepository;
import com.example.assets.infrastructure.database.repository.custom.AssetCustomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class AssetRepositoryAdapter implements AssetRepositoryPort {

    private final AssetR2dbcRepository assetR2dbcRepository;
    private final AssetCustomRepository assetCustomRepository;
    private final AssetEntityMapper assetEntityMapper;

    @Override
    public Mono<Asset> save(final Asset asset) {
        final AssetEntity entity = assetEntityMapper.toEntity(asset);
        entity.setCreatedAt(Instant.now());
        entity.setStatus(asset.getStatus() != null ? asset.getStatus().name() : null);
        entity.setNewAsset(true);
        return assetR2dbcRepository.save(entity).map(assetEntityMapper::toDomain);
    }

    @Override
    public Flux<Asset> findByFilter(final String filename, final String contentType, final String sortBy, final String sortDirection) {
        return assetCustomRepository.findByFilter(filename, contentType, sortBy, sortDirection)
                .map(assetEntityMapper::toDomain);
    }

    @Override
    public Mono<Void> updateMetadataAndStatus(final String id, final String url, final Long size,
                                              final Instant uploadedAt, final String errorMessage) {
        return assetR2dbcRepository.findById(id)
                .flatMap(entity -> {
                    if (errorMessage == null) {
                        entity.setStatus(AssetStatus.PUBLISHED.name());
                        entity.setUrl(url);
                        entity.setSize(size);
                        entity.setUploadedAt(uploadedAt);
                    } else {
                        entity.setStatus(AssetStatus.FAILED.name());
                    }
                    entity.setUpdatedAt(Instant.now());
                    return assetR2dbcRepository.save(entity);
                })
                .then();
    }
}
