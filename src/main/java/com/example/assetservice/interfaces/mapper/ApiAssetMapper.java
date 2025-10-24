package com.example.assetservice.interfaces.mapper;

import com.example.assetservice.domain.model.Asset;
import com.example.assetservice.interfaces.dto.AssetRequestDto;
import com.example.assetservice.interfaces.dto.AssetResponseDto;
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
