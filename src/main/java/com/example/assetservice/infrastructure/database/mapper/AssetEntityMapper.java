package com.example.assetservice.infrastructure.database.mapper;

import com.example.assetservice.domain.model.Asset;
import com.example.assetservice.infrastructure.database.entity.AssetEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AssetEntityMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", source = "status")
    AssetEntity toEntity(Asset asset);

    @Mapping(target = "status", expression = "java(assetEntity.getStatus() != null ? com.example.assetservice.domain.model.AssetStatus.valueOf(assetEntity.getStatus()) : null)")
    Asset toDomain(AssetEntity assetEntity);
}
