package com.inditex.assets.interfaces.web.adapter.mapper;

import com.inditex.assets.domain.model.Asset;
import com.inditex.assets.interfaces.web.model.asset.AssetDto;
import com.inditex.assets.interfaces.web.model.asset.AssetUploadRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface AssetInterfaceMapper {

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
