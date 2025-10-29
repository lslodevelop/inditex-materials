package com.inditex.assets.infrastructure.database.repository;

import com.inditex.assets.infrastructure.database.entity.AssetEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface AssetR2dbcRepository extends ReactiveCrudRepository<AssetEntity, String> {

}
