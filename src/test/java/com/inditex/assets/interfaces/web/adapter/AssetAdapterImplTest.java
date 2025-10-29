package com.inditex.assets.interfaces.web.adapter;

import com.inditex.assets.domain.model.Asset;
import com.inditex.assets.domain.port.in.UploadAssetPort;
import com.inditex.assets.interfaces.web.adapter.impl.AssetAdapterImpl;
import com.inditex.assets.interfaces.web.adapter.mapper.AssetInterfaceMapper;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetAdapterImplTest {

    @InjectMocks
    private AssetAdapterImpl assetAdapterImpl;

    @Mock
    private UploadAssetPort uploadAssetPort;

    @Mock
    private AssetInterfaceMapper assetInterfaceMapper;

    @Test
    void uploadTest() {
        //given
        final AssetUploadRequestDto assetUploadRequestDto =
                AssetUploadRequestDto.builder()
                        .encodedFile("ZHVtbXkgZmlsZQ==")
                        .build();
        final Asset asset = Asset.builder().build();
        final String expectedId = UUID.randomUUID().toString();
        final Mono<String> fileIdMono = Mono.just(expectedId);

        when(assetInterfaceMapper.toDomain(assetUploadRequestDto)).thenReturn(asset);
        when(uploadAssetPort.upload(asset, assetUploadRequestDto.getEncodedFile())).thenReturn(fileIdMono);

        //when
        final Mono<AssetUploadResponseDto> result = assetAdapterImpl.upload(assetUploadRequestDto);

        //then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getId()).isEqualTo(expectedId);
                })
                .verifyComplete();

        verify(uploadAssetPort).upload(asset, assetUploadRequestDto.getEncodedFile());
        verify(assetInterfaceMapper).toDomain(assetUploadRequestDto);
        verifyNoMoreInteractions(assetInterfaceMapper, uploadAssetPort);
    }

    @Test
    void searchTest() {
        //given
        final String filename = "test.txt";
        final String contentType = "text/plain";
        final String sortBy = "createdAt";
        final String sortDirection = "DESC";
        final Asset asset = Asset.builder().build();
        final AssetDto assetDto = AssetDto.builder().build();
        final Flux<Asset> assetFlux = Flux.just(asset);

        when(uploadAssetPort.search(filename, contentType, sortBy, sortDirection)).thenReturn(assetFlux);
        when(assetInterfaceMapper.toDto(asset)).thenReturn(assetDto);

        //when
        final Flux<AssetDto> result = assetAdapterImpl.search(filename, contentType, sortBy, sortDirection);

        //then
        final List<AssetDto> assetDtoList = new ArrayList<>();
        StepVerifier.create(result)
                .consumeNextWith(assetDtoList::add)
                .expectComplete()
                .verifyThenAssertThat()
                .hasNotDroppedErrors()
                .hasNotDroppedElements();

        assertThat(assetDtoList).hasSize(1);
        verify(uploadAssetPort).search(filename, contentType, sortBy, sortDirection);
        verify(assetInterfaceMapper).toDto(asset);
        verifyNoMoreInteractions(assetInterfaceMapper, uploadAssetPort);
    }

}