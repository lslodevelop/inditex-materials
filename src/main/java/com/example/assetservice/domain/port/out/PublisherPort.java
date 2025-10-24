package com.example.assetservice.domain.port.out;

import com.example.assetservice.domain.model.Asset;
import reactor.core.publisher.Mono;

public interface PublisherPort {
    Mono<Void> publish(Asset asset, String encodedFile);
}
