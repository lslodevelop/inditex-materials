package com.inditex.assets.interfaces.web.adapter;

import com.inditex.assets.domain.model.Asset;
import com.inditex.assets.domain.port.in.UploadAssetPort;
import com.inditex.assets.interfaces.web.mapper.ApiAssetMapper;
import com.inditex.assets.interfaces.web.model.asset.AssetDto;
import com.inditex.assets.interfaces.web.model.asset.AssetUploadRequestDto;
import com.inditex.assets.interfaces.web.model.asset.AssetUploadResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AssetInputAdapter {

    private final UploadAssetPort uploadAssetPort;
    private final ApiAssetMapper apiAssetMapper;

    public Mono<AssetUploadResponseDto> upload(final AssetUploadRequestDto assetUploadRequestDto) {
        final Asset domain = apiAssetMapper.toDomain(assetUploadRequestDto);
        return uploadAssetPort.upload(domain, assetUploadRequestDto.getEncodedFile())
                .map(id -> AssetUploadResponseDto.builder().id(id).build());
    }

    public Flux<AssetDto> search(final String filename, final String contentType, final String sortBy, final String sortDirection) {
        return uploadAssetPort.search(filename, contentType, sortBy, sortDirection)
                .map(apiAssetMapper::toDto);
    }
}
