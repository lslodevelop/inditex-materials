package com.example.assets.domain.port.out;

import com.example.assets.domain.model.Asset;
import reactor.core.publisher.Mono;

public interface StorageClientPort {
    Mono<Void> publish(Asset asset, String encodedFile);
}
