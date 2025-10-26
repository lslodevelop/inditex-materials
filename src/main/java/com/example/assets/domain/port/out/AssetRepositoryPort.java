package com.example.assets.domain.port.out;

import com.example.assets.domain.model.Asset;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.time.Instant;

public interface AssetRepositoryPort {

    Mono<Asset> save(Asset asset);
    Flux<Asset> findByFilter(String filename, String contentType, String sortBy, String sortDirection);
    Mono<Void> updateMetadataAndStatus(String id, String url, Long size, Instant uploadedAt, String errorMessage);

}
