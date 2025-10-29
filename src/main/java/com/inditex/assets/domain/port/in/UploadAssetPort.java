package com.inditex.assets.domain.port.in;

import com.inditex.assets.domain.model.Asset;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UploadAssetPort {

    Mono<String> upload(Asset asset, String encodedFile);
    Flux<Asset> search(String filename, String contentType, String sortBy, String sortDirection);

}
