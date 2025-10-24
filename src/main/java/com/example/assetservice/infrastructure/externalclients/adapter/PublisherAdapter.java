package com.example.assetservice.infrastructure.externalclients.adapter;

import com.example.assetservice.domain.model.Asset;
import com.example.assetservice.domain.port.out.PublisherPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class PublisherAdapter implements PublisherPort {
    @Override
    public Mono<Void> publish(Asset asset, String encodedFile) {
        log.info("[MOCK PUBLISHER] assetId={} filename={}", asset.getId(), asset.getFilename());
        // Simular proceso asíncrono:
        return Mono.empty();
    }
}
