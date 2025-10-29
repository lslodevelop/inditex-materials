package com.inditex.assets.interfaces.web.adapter.impl;

import com.inditex.assets.domain.model.Asset;
import com.inditex.assets.domain.port.in.UploadAssetPort;
import com.inditex.assets.interfaces.web.adapter.AssetAdapter;
import com.inditex.assets.interfaces.web.adapter.mapper.AssetInterfaceMapper;
import com.inditex.assets.interfaces.web.model.asset.AssetDto;
import com.inditex.assets.interfaces.web.model.asset.AssetUploadRequestDto;
import com.inditex.assets.interfaces.web.model.asset.AssetUploadResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AssetAdapterImpl implements AssetAdapter {

    private final UploadAssetPort uploadAssetPort;
    private final AssetInterfaceMapper assetInterfaceMapper;

    public Mono<AssetUploadResponseDto> upload(final AssetUploadRequestDto assetUploadRequestDto) {
        final Asset domain = assetInterfaceMapper.toDomain(assetUploadRequestDto);
        return uploadAssetPort.upload(domain, assetUploadRequestDto.getEncodedFile())
                .map(id -> AssetUploadResponseDto.builder().id(id).build());
    }

    public Flux<AssetDto> search(final String filename, final String contentType, final String sortBy, final String sortDirection) {
        return uploadAssetPort.search(filename, contentType, sortBy, sortDirection)
                .map(assetInterfaceMapper::toDto);
    }
}
