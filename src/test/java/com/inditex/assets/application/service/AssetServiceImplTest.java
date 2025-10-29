package com.inditex.assets.application.service;

import com.inditex.assets.domain.model.Asset;
import com.inditex.assets.domain.model.AssetStatus;
import com.inditex.assets.domain.port.out.AssetRepositoryPort;
import com.inditex.assets.domain.port.out.StorageClientPort;
import com.inditex.assets.infrastructure.storage.model.StorageMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetServiceImplTest {

    @InjectMocks
    private AssetServiceImpl assetService;

    @Mock
    private AssetRepositoryPort assetRepositoryPort;

    @Mock
    private StorageClientPort storageClientPort;

    @Test
    void uploadTest() {
        // given
        final Asset asset = Asset.builder().build();
        final String encodedFile = "encodedFile";

        final StorageMetadata storageMetadata = StorageMetadata.builder()
                .url("url")
                .size(1L)
                .uploadedAt(Instant.now())
                .build();

        final ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);

        when(assetRepositoryPort.save(assetCaptor.capture()))
                .thenReturn(Mono.just(asset));
        when(storageClientPort.upload(any(Asset.class), eq(encodedFile)))
                .thenReturn(Mono.just(storageMetadata));
        when(assetRepositoryPort.updateMetadataAndStatus(
                anyString(),
                eq(storageMetadata.getUrl()),
                eq(storageMetadata.getSize()),
                eq(storageMetadata.getUploadedAt()),
                isNull()
        )).thenReturn(Mono.empty());

        // when
        final Mono<String> result = assetService.upload(asset, encodedFile);

        // then
        StepVerifier.create(result)
                .assertNext(id -> {
                    assertThat(id).isNotNull();
                    final Asset savedAsset = assetCaptor.getValue();
                    assertThat(savedAsset.getId()).isEqualTo(id);
                    assertThat(savedAsset.getStatus()).isEqualTo(AssetStatus.PENDING);
                    assertThat(savedAsset.getCreatedAt()).isNotNull();

                    verify(assetRepositoryPort).updateMetadataAndStatus(
                            id,
                            storageMetadata.getUrl(),
                            storageMetadata.getSize(),
                            storageMetadata.getUploadedAt(),
                            null
                    );
                })
                .verifyComplete();

        verify(assetRepositoryPort).save(any(Asset.class));
        verify(storageClientPort).upload(any(Asset.class), eq(encodedFile));
        verifyNoMoreInteractions(assetRepositoryPort, storageClientPort);

    }

    @Test
    void uploadErrorTest() {
        // given
        final Asset asset = Asset.builder().build();
        final String encodedFile = "encodedFile";
        final String errorMessage = "Simulated upload error";

        final ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);

        when(assetRepositoryPort.save(assetCaptor.capture()))
                .thenReturn(Mono.just(asset));
        when(storageClientPort.upload(asset, encodedFile))
                .thenReturn(Mono.error(new RuntimeException(errorMessage)));
        when(assetRepositoryPort.updateMetadataAndStatus(
                anyString(),
                isNull(),
                eq(0L),
                isNull(),
                eq(errorMessage)
        )).thenReturn(Mono.empty());

        // when
        final Mono<String> result = assetService.upload(asset, encodedFile);

        // then
        StepVerifier.create(result)
                .assertNext(id -> {
                    assertThat(id).isNotNull();
                    assertThat(id).isEqualTo(asset.getId());
                })
                .verifyComplete();

        // verify
        verify(assetRepositoryPort).save(asset);
        verify(storageClientPort).upload(asset, encodedFile);
        verify(assetRepositoryPort).updateMetadataAndStatus(
                eq(asset.getId()),
                isNull(),
                eq(0L),
                isNull(),
                eq(errorMessage)
        );
        verifyNoMoreInteractions(assetRepositoryPort, storageClientPort);
    }

    @Test
    void searchTest() {
        //given
        final String filename = "test.txt";
        final String contentType = "text/plain";
        final String sortBy = "createdAt";
        final String sortDirection = "DESC";
        final Asset asset = Asset.builder()
                .filename("test.txt")
                .build();
        final Flux<Asset> assetFlux = Flux.just(asset);
        final String fileContent = "fileContent";
        final Mono<String> fileContentMono = Mono.just(fileContent);

        when(assetRepositoryPort.findByFilter(filename, contentType, sortBy, sortDirection)).thenReturn(assetFlux);
        when(storageClientPort.getFile(asset.getFilename())).thenReturn(fileContentMono);

        //when
        final Flux<Asset> result = assetService.search(filename, contentType, sortBy, sortDirection);

        //then
        final List<Asset> assetList = new ArrayList<>();
        StepVerifier.create(result)
                .consumeNextWith(assetList::add)
                .expectComplete()
                .verifyThenAssertThat()
                .hasNotDroppedErrors()
                .hasNotDroppedElements();

        assertThat(assetList).hasSize(1);
        assertThat(assetList.get(0).getEncodedFile()).isEqualTo(fileContent);
        verify(assetRepositoryPort).findByFilter(filename, contentType, sortBy, sortDirection);
        verify(storageClientPort).getFile(asset.getFilename());

        verifyNoMoreInteractions(assetRepositoryPort, storageClientPort);
    }

    @Test
    void searchFileNotRetrievedTest() {
        //given
        final String filename = "test.txt";
        final String contentType = "text/plain";
        final String sortBy = "createdAt";
        final String sortDirection = "DESC";
        final Asset asset = Asset.builder()
                .filename("test.txt")
                .build();
        final Flux<Asset> assetFlux = Flux.just(asset);

        when(assetRepositoryPort.findByFilter(filename, contentType, sortBy, sortDirection)).thenReturn(assetFlux);
        when(storageClientPort.getFile(asset.getFilename()))
                .thenReturn(Mono.error(new RuntimeException("Simulated error")));
        //when
        final Flux<Asset> result = assetService.search(filename, contentType, sortBy, sortDirection);

        //then
        final List<Asset> assetList = new ArrayList<>();
        StepVerifier.create(result)
                .consumeNextWith(assetList::add)
                .expectComplete()
                .verifyThenAssertThat()
                .hasNotDroppedErrors()
                .hasNotDroppedElements();

        assertThat(assetList).hasSize(1);
        assertThat(assetList.get(0).getEncodedFile()).isNull();
        verify(assetRepositoryPort).findByFilter(filename, contentType, sortBy, sortDirection);
        verify(storageClientPort).getFile(asset.getFilename());

        verifyNoMoreInteractions(assetRepositoryPort, storageClientPort);
    }
}