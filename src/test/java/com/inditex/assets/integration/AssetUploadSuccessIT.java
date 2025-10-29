package com.inditex.assets.integration;

import com.inditex.assets.infrastructure.database.repository.AssetR2dbcRepository;
import com.inditex.assets.interfaces.web.model.asset.AssetUploadRequestDto;
import com.inditex.assets.interfaces.web.model.asset.AssetUploadResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "local"})
@AutoConfigureWebTestClient
class AssetUploadSuccessIT extends BaseIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private AssetR2dbcRepository assetR2dbcRepository;

    @Test
    void uploadEndpointShouldReturnIdAndPersistAssetTest() {

        final AssetUploadRequestDto request = AssetUploadRequestDto.builder()
                .filename("test.txt")
                .contentType("text/plain")
                .encodedFile("ZHVtbXkgZmlsZQ==")
                .build();

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

        StepVerifier.create(assetR2dbcRepository.findById(assetUploadResponseDto.getId()))
                .assertNext(asset -> {
                    assertThat(asset.getFilename()).isEqualTo("test.txt");
                    assertThat(asset.getContentType()).isEqualTo("text/plain");
                    assertThat(asset.getStatus()).isEqualTo("PUBLISHED");
                    assertThat(asset.getCreatedAt()).isNotNull();
                })
                .verifyComplete();
    }

}
