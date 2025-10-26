package com.example.assets.interfaces.mapper;

import com.example.assets.domain.model.Asset;
import com.example.assets.interfaces.model.asset.AssetDto;
import com.example.assets.interfaces.model.asset.AssetUploadRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface ApiAssetMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    Asset toDomain(AssetUploadRequestDto dto);

    @Mapping(target = "uploadDate", expression = "java(toLocalDateTime(asset.getUploadedAt()))")
    AssetDto toDto(Asset asset);

    default LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null :
                LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

}
