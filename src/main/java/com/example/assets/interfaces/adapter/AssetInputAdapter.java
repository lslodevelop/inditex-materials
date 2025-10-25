package com.example.assets.interfaces.adapter;

import com.example.assets.domain.model.Asset;
import com.example.assets.domain.port.in.UploadAssetUseCase;
import com.example.assets.interfaces.model.asset.AssetRequestDto;
import com.example.assets.interfaces.model.asset.AssetResponseDto;
import com.example.assets.interfaces.mapper.ApiAssetMapper;
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
