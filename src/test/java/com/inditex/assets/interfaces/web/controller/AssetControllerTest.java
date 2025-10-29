package com.inditex.assets.interfaces.web.controller;

import com.inditex.assets.interfaces.web.adapter.impl.AssetAdapterImpl;
import com.inditex.assets.interfaces.web.model.asset.AssetDto;
import com.inditex.assets.interfaces.web.model.asset.AssetUploadRequestDto;
import com.inditex.assets.interfaces.web.model.asset.AssetUploadResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetControllerTest {

    @InjectMocks
    private AssetController assetController;

    @Mock
    private AssetAdapterImpl assetAdapterImpl;

    @Test
    void uploadTest() {
        //given
        final AssetUploadRequestDto assetUploadRequestDto = AssetUploadRequestDto.builder().build();
        final AssetUploadResponseDto assetUploadResponseDto = AssetUploadResponseDto.builder()
                .id("test-id")
                .build();

        final Mono<AssetUploadResponseDto> assetUploadResponseDtoMono = Mono.just(assetUploadResponseDto);
        when(assetAdapterImpl.upload(assetUploadRequestDto)).thenReturn(assetUploadResponseDtoMono);

        //when
        final Mono<AssetUploadResponseDto> result = assetController.upload(assetUploadRequestDto);

        //then
        StepVerifier.create(result)
                .assertNext(response -> assertThat(response).isNotNull())
                .verifyComplete();

        verify(assetAdapterImpl).upload(assetUploadRequestDto);
        verifyNoMoreInteractions(assetAdapterImpl);
    }

    @Test
    void searchTest() {
        //given
        final String filename = "test.txt";
        final String contentType = "text/plain";
        final String sortBy = "createdAt";
        final String sortDirection = "DESC";

        final AssetDto assetDto = AssetDto.builder().build();
        final Flux<AssetDto> assetDtoFlux = Flux.just(assetDto);

        when(assetAdapterImpl.search(filename, contentType, sortBy, sortDirection)).thenReturn(assetDtoFlux);

        //when
        final Flux<AssetDto> result = assetController.search(filename, contentType, sortBy, sortDirection);

        //then
        StepVerifier.create(result)
                .assertNext(response -> assertThat(response).isNotNull())
                .verifyComplete();

        verify(assetAdapterImpl).search(filename, contentType, sortBy, sortDirection);
        verifyNoMoreInteractions(assetAdapterImpl);
    }

}