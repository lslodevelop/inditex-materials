package com.example.assetservice.infrastructure.database.repository;

import com.example.assetservice.infrastructure.database.entity.AssetEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface AssetR2dbcRepository extends ReactiveCrudRepository<AssetEntity, String> {

    Flux<AssetEntity> findByFilenameContainingIgnoreCase(String filename);
    Flux<AssetEntity> findByContentType(String contentType);

}
