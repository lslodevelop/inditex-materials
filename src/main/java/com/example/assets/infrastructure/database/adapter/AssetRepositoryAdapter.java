package com.example.assets.infrastructure.database.adapter;

import com.example.assets.domain.model.Asset;
import com.example.assets.domain.model.AssetStatus;
import com.example.assets.domain.port.out.AssetRepositoryPort;
import com.example.assets.infrastructure.database.mapper.AssetEntityMapper;
import com.example.assets.infrastructure.database.entity.AssetEntity;
import com.example.assets.infrastructure.database.repository.AssetR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class AssetRepositoryAdapter implements AssetRepositoryPort {

    private final AssetR2dbcRepository repo;
    private final AssetEntityMapper mapper;

    @Override
    public Mono<Asset> save(final Asset asset) {
        AssetEntity entity = mapper.toEntity(asset);
        entity.setCreatedAt(Instant.now());
        entity.setStatus(asset.getStatus() != null ? asset.getStatus().name() : null);
        entity.setNewAsset(true);
        return repo.save(entity).map(mapper::toDomain);
    }

    @Override
    public Mono<Asset> findById(final String id) {
        return repo.findById(id).map(mapper::toDomain);
    }

    @Override
    public Flux<Asset> findByFilter(final String filename, final String contentType) {
        if (filename != null && !filename.isBlank()) {
            return repo.findByFilenameContainingIgnoreCase(filename).map(mapper::toDomain);
        }
        if (contentType != null && !contentType.isBlank()) {
            return repo.findByContentType(contentType).map(mapper::toDomain);
        }
        return repo.findAll().map(mapper::toDomain);
    }

    @Override
    public Mono<Void> updateStatus(final String id, final String errorMessage) {
        return repo.findById(String.valueOf(id))
                .flatMap(entity -> {
                    entity.setStatus(errorMessage == null ? AssetStatus.PUBLISHED.name() : AssetStatus.FAILED.name());
                    entity.setUpdatedAt(Instant.now());
                    return repo.save(entity);
                })
                .then();
    }
}
