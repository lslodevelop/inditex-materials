package com.example.assets.domain.port.in;

import com.example.assets.domain.model.Asset;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UploadAssetUseCase {

    Mono<String> upload(Asset asset, String encodedFile);
    Flux<Asset> search(String filename, String contentType);

}
