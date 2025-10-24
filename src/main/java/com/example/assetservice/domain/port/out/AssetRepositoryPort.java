package com.example.assetservice.domain.port.out;

import com.example.assetservice.domain.model.Asset;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

public interface AssetRepositoryPort {

    Mono<Asset> save(Asset asset);
    Mono<Asset> findById(String id);
    Flux<Asset> findByFilter(String filename, String contentType);
    Mono<Void> updateStatus(String id, String url, Long size, String errorMessage);

}
