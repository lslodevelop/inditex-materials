package com.inditex.assets.infrastructure.database.adapter;

import com.inditex.assets.domain.model.Asset;
import com.inditex.assets.domain.model.AssetStatus;
import com.inditex.assets.infrastructure.database.adapter.mapper.AssetEntityMapper;
import com.inditex.assets.infrastructure.database.entity.AssetEntity;
import com.inditex.assets.infrastructure.database.repository.AssetR2dbcRepository;
import com.inditex.assets.infrastructure.database.repository.custom.AssetCustomRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetRepositoryAdapterImplTest {

    @InjectMocks
    private AssetRepositoryAdapterImpl assetRepositoryAdapterImpl;

    @Mock
    private AssetR2dbcRepository assetR2dbcRepository;

    @Mock
    private AssetCustomRepository assetCustomRepository;

    @Mock
    private AssetEntityMapper assetEntityMapper;

    @Test
    void saveTest() {
        //given
        final Asset asset = Asset.builder()
                .status(AssetStatus.PENDING)
                .build();
        final AssetEntity assetEntity = new AssetEntity();

        when(assetEntityMapper.toEntity(asset)).thenReturn(assetEntity);
        when(assetEntityMapper.toDomain(assetEntity)).thenReturn(asset);
        when(assetR2dbcRepository.save(assetEntity)).thenReturn(Mono.just(assetEntity));

        //when
        final Mono<Asset> result = assetRepositoryAdapterImpl.save(asset);

        //then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getStatus()).isEqualTo(AssetStatus.PENDING);
                })
                .verifyComplete();

        verify(assetEntityMapper).toEntity(asset);
        verify(assetEntityMapper).toDomain(assetEntity);
        verify(assetR2dbcRepository).save(assetEntity);
    }

    @Test
    void findByFilterTest() {
        //given
        final String filename = "test.txt";
        final String contentType = "text/plain";
        final String sortBy = "createdAt";
        final String sortDirection = "DESC";
        final AssetEntity assetEntity = new AssetEntity();
        final Asset asset = Asset.builder().build();
        final Flux<AssetEntity> assetEntityFlux = Flux.just(assetEntity);

        when(assetCustomRepository.findByFilter(filename, contentType, sortBy, sortDirection)).thenReturn(assetEntityFlux);
        when(assetEntityMapper.toDomain(assetEntity)).thenReturn(asset);

        //when
        final Flux<Asset> result = assetRepositoryAdapterImpl.findByFilter(filename, contentType, sortBy, sortDirection);

        //then
        final List<Asset> assetList = new ArrayList<>();
        StepVerifier.create(result)
                .consumeNextWith(assetList::add)
                .expectComplete()
                .verifyThenAssertThat()
                .hasNotDroppedErrors()
                .hasNotDroppedElements();

        assertThat(assetList).hasSize(1);
        verify(assetCustomRepository).findByFilter(filename, contentType, sortBy, sortDirection);
        verify(assetEntityMapper).toDomain(assetEntity);

    }

    @Test
    void updateMetadataAndStatusNoErrorMessageTest() {
        // given
        final String id = "id";
        final String url = "url";
        final Long size = 100L;
        final Instant uploadedAt = Instant.now();
        final AssetEntity assetEntity = new AssetEntity();
        final ArgumentCaptor<AssetEntity> captor = ArgumentCaptor.forClass(AssetEntity.class);

        when(assetR2dbcRepository.findById(id)).thenReturn(Mono.just(assetEntity));
        when(assetR2dbcRepository.save(captor.capture())).thenReturn(Mono.just(new AssetEntity()));

        // when
        Mono<Void> result = assetRepositoryAdapterImpl.updateMetadataAndStatus(id, url, size, uploadedAt, null);

        // then
        StepVerifier.create(result)
                .verifyComplete();

        final AssetEntity assetEntitySaved = captor.getValue();
        assertThat(assetEntitySaved.getStatus()).isEqualTo(AssetStatus.PUBLISHED.name());
        assertThat(assetEntitySaved.getUrl()).isEqualTo(url);
        assertThat(assetEntitySaved.getSize()).isEqualTo(size);
        assertThat(assetEntitySaved.getUploadedAt()).isEqualTo(uploadedAt);

        verify(assetR2dbcRepository).findById(id);
        verify(assetR2dbcRepository).save(captor.capture());
        verifyNoMoreInteractions(assetR2dbcRepository);
    }

    @Test
    void updateMetadataAndStatusErrorTest() {
        // given
        final String id = "id";
        final String url = "url";
        final Long size = 100L;
        final Instant uploadedAt = Instant.now();
        final String errorMessage = "error message";
        final AssetEntity assetEntity = new AssetEntity();
        final ArgumentCaptor<AssetEntity> captor = ArgumentCaptor.forClass(AssetEntity.class);

        when(assetR2dbcRepository.findById(id)).thenReturn(Mono.just(assetEntity));
        when(assetR2dbcRepository.save(captor.capture())).thenReturn(Mono.just(new AssetEntity()));

        // when
        Mono<Void> result = assetRepositoryAdapterImpl.updateMetadataAndStatus(id, url, size, uploadedAt, errorMessage);

        // then
        StepVerifier.create(result)
                .verifyComplete();

        final AssetEntity assetEntitySaved = captor.getValue();
        assertThat(assetEntitySaved.getStatus()).isEqualTo(AssetStatus.FAILED.name());
        assertThat(assetEntitySaved.getUrl()).isNull();
        assertThat(assetEntitySaved.getSize()).isNull();
        assertThat(assetEntitySaved.getUploadedAt()).isNull();

        verify(assetR2dbcRepository).findById(id);
        verify(assetR2dbcRepository).save(captor.capture());
        verifyNoMoreInteractions(assetR2dbcRepository);
    }

}