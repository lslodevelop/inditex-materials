package com.example.assetservice.interfaces.adapter;

import com.example.assetservice.domain.model.Asset;
import com.example.assetservice.domain.port.in.UploadAssetUseCase;
import com.example.assetservice.interfaces.dto.AssetRequestDto;
import com.example.assetservice.interfaces.dto.AssetResponseDto;
import com.example.assetservice.interfaces.mapper.ApiAssetMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class AssetInputAdapter {

    private final UploadAssetUseCase uploadUseCase;
    private final ApiAssetMapper apiAssetMapper;

    public Mono<AssetResponseDto> upload(final AssetRequestDto dto) {
        final Asset domain = apiAssetMapper.toDomain(dto);
        return uploadUseCase.upload(domain, dto.getEncodedFile())
                .map(id -> AssetResponseDto.builder().id(id).build());
    }

    public Flux<AssetResponseDto> search(final String filename, final String contentType) {
        return uploadUseCase.search(filename, contentType)
                .map(apiAssetMapper::toDto);
    }
}
