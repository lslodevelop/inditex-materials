package com.inditex.assets.infrastructure.database.repository.custom.impl;

import com.inditex.assets.infrastructure.database.entity.AssetEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetCustomRepositoryImplTest {

    @InjectMocks
    private AssetCustomRepositoryImpl assetCustomRepository;

    @Mock
    private R2dbcEntityTemplate r2dbcEntityTemplate;

    @Test
    void findByFilterWithAllParamsTest() {
        // given
        final String filename = "image";
        final String contentType = "image/png";
        final String sortBy = "filename";
        final String sortDirection = "DESC";

        final AssetEntity asset = new AssetEntity();
        asset.setFilename("image_123.png");

        final ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);

        when(r2dbcEntityTemplate.select(queryCaptor.capture(), eq(AssetEntity.class)))
                .thenReturn(Flux.just(asset));

        // when
        Flux<AssetEntity> result = assetCustomRepository.findByFilter(filename, contentType, sortBy, sortDirection);

        // then
        StepVerifier.create(result)
                .expectNext(asset)
                .verifyComplete();

        verify(r2dbcEntityTemplate).select(queryCaptor.capture(), eq(AssetEntity.class));
        final Query capturedQuery = queryCaptor.getValue();

        // assertions on query
        assertThat(capturedQuery.getCriteria().isPresent());
        final String queryStr = capturedQuery.getCriteria().get().toString();
        final String capturedOrder = capturedQuery.getSort().toString();
        assertThat(queryStr).contains("filename LIKE '%image%'");
        assertThat(queryStr).contains("content_type = 'image/png'");
        assertThat(capturedOrder).isEqualTo("filename: DESC");
    }

    @Test
    void findByFilterWithNoSearchCriteriaAndInvalidSortFieldTest() {
        // given
        final String sortBy = "invalidField";
        final ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);

        when(r2dbcEntityTemplate.select(any(Query.class), eq(AssetEntity.class)))
                .thenReturn(Flux.empty());

        // when
        final Flux<AssetEntity> result = assetCustomRepository.findByFilter(null, null, sortBy, "ASC");

        // then
        StepVerifier.create(result).verifyComplete();

        verify(r2dbcEntityTemplate).select(queryCaptor.capture(), eq(AssetEntity.class));
        final Query capturedQuery = queryCaptor.getValue();
        final String sortField = capturedQuery.getSort().toString();
        assertThat(sortField).contains("created_at: ASC");
        assertThat(capturedQuery.getCriteria().isPresent());
        assertThat(capturedQuery.getCriteria().get().isEmpty());
    }

}