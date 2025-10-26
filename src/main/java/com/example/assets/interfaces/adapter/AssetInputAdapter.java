package com.example.assets.interfaces.adapter;

import com.example.assets.domain.model.Asset;
import com.example.assets.domain.port.in.UploadAssetUseCase;
import com.example.assets.interfaces.model.asset.AssetDto;
import com.example.assets.interfaces.model.asset.AssetUploadRequestDto;
import com.example.assets.interfaces.model.asset.AssetUploadResponseDto;
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

    public Mono<AssetUploadResponseDto> upload(final AssetUploadRequestDto dto) {
        final Asset domain = apiAssetMapper.toDomain(dto);
        return uploadUseCase.upload(domain, dto.getEncodedFile())
                .map(id -> AssetUploadResponseDto.builder().id(id).build());
    }

    public Flux<AssetDto> search(final String filename, final String contentType, final String sortBy, final String sortDirection) {
        return uploadUseCase.search(filename, contentType, sortBy, sortDirection)
                .map(apiAssetMapper::toDto);
    }
}
