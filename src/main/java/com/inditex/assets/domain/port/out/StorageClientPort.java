package com.inditex.assets.domain.port.out;

import com.inditex.assets.domain.model.Asset;
import com.inditex.assets.infrastructure.storage.model.StorageMetadata;
import reactor.core.publisher.Mono;

public interface StorageClientPort {

    Mono<String> getFile(String filename);
    Mono<StorageMetadata> upload(Asset asset, String encodedFile);

}
