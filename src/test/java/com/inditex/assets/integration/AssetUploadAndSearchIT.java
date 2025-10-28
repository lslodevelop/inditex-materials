package com.inditex.assets.integration;

import com.inditex.assets.domain.port.out.StorageClientPort;
import com.inditex.assets.infrastructure.storage.model.StorageMetadata;
import com.inditex.assets.interfaces.web.model.asset.AssetDto;
import com.inditex.assets.interfaces.web.model.asset.AssetUploadRequestDto;
import com.inditex.assets.interfaces.web.model.asset.AssetUploadResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "local"})
@AutoConfigureWebTestClient
class AssetUploadAndSearchIT extends BaseIT {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private StorageClientPort storageClientPort;

    @Test
    void shouldUploadFileAndThenRetrieveItSuccessfully() {
        // given
        final AssetUploadRequestDto request = AssetUploadRequestDto.builder()
                .filename("integration-test.txt")
                .contentType("text/plain")
                .encodedFile("ZHVtbXkgZmlsZQ==")
                .build();

        when(storageClientPort.upload(any(), eq("ZHVtbXkgZmlsZQ==")))
                .thenReturn(Mono.just(
                        StorageMetadata.builder()
                                .url("http://localhost:9000/assets/integration-test.txt")
                                .size(12L)
                                .uploadedAt(Instant.now())
                                .build()
                ));

        when(storageClientPort.getFile("integration-test.txt"))
                .thenReturn(Mono.just(Base64.getEncoder().encodeToString("ZHVtbXkgZmlsZQ==".getBytes())));

        // when
        final AssetUploadResponseDto assetUploadResponseDto = webTestClient.post()
                .uri("/api/mgmt/1/assets/actions/upload")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody(AssetUploadResponseDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(assetUploadResponseDto)
                .isNotNull()
                .extracting(AssetUploadResponseDto::getId)
                .isNotNull();

        // then
        final Flux<AssetDto> resultFlux = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/mgmt/1/assets")
                        .queryParam("filename", "integration-test.txt")
                        .queryParam("contentType", "text/plain")
                        .queryParam("sortBy", "createdAt")
                        .queryParam("sortDirection", "DESC")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .returnResult(AssetDto.class)
                .getResponseBody();

        StepVerifier.create(resultFlux)
                .assertNext(asset -> {
                    assertThat(asset).isNotNull();
                    assertThat(asset.getId()).isNotNull();
                    assertThat(asset.getFilename()).isEqualTo(request.getFilename());
                    assertThat(asset.getContentType()).isEqualTo(request.getContentType());
                    assertThat(asset.getStatus()).isEqualTo("PUBLISHED");
                    assertThat(asset.getEncodedFile()).isNotNull();
                    assertThat(asset.getUploadDate()).isNotNull();
                    assertThat(asset.getUrl()).isNotEmpty();
                })
                .thenConsumeWhile(a -> true)
                .verifyComplete();
    }
}
