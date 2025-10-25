package com.example.assets.infrastructure.database.mapper;

import com.example.assets.domain.model.Asset;
import com.example.assets.infrastructure.database.entity.AssetEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AssetEntityMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", source = "status")
    AssetEntity toEntity(Asset asset);

    @Mapping(target = "status", expression = "java(assetEntity.getStatus() != null ? com.example.assets.domain.model.AssetStatus.valueOf(assetEntity.getStatus()) : null)")
    Asset toDomain(AssetEntity assetEntity);
}
