package com.example.assets.infrastructure.database.repository.custom;

import com.example.assets.infrastructure.database.entity.AssetEntity;
import reactor.core.publisher.Flux;

public interface AssetCustomRepository {

    Flux<AssetEntity> findByFilter(String filename, String contentType, String sortBy, String sortDirection);

}
