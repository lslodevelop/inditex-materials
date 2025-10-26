package com.example.assets.domain.port.out;

import com.example.assets.domain.model.Asset;
import com.example.assets.infrastructure.storage.model.StorageMetadata;
import reactor.core.publisher.Mono;

public interface StorageClientPort {

    Mono<String> getFile(String filename);
    Mono<StorageMetadata> upload(Asset asset, String encodedFile);

}
