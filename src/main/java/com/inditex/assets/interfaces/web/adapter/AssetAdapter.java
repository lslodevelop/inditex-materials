package com.inditex.assets.interfaces.web.adapter;

import com.inditex.assets.interfaces.web.model.asset.AssetDto;
import com.inditex.assets.interfaces.web.model.asset.AssetUploadRequestDto;
import com.inditex.assets.interfaces.web.model.asset.AssetUploadResponseDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AssetAdapter {

    Mono<AssetUploadResponseDto> upload(AssetUploadRequestDto assetUploadRequestDto);
    Flux<AssetDto> search(String filename, String contentType, String sortBy, String sortDirection);

}
