package com.example.assets.interfaces.mapper;

import com.example.assets.domain.model.Asset;
import com.example.assets.interfaces.model.asset.AssetRequestDto;
import com.example.assets.interfaces.model.asset.AssetResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ApiAssetMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    Asset toDomain(AssetRequestDto dto);

    AssetResponseDto toDto(Asset asset);
}
