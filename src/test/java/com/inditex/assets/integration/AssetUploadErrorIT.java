package com.inditex.assets.integration;

import com.inditex.assets.domain.exception.GenericErrorCodes;
import com.inditex.assets.domain.port.out.AssetRepositoryPort;
import com.inditex.assets.interfaces.web.model.ErrorResponseDto;
import com.inditex.assets.interfaces.web.model.asset.AssetUploadRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "local"})
@AutoConfigureWebTestClient
class AssetUploadErrorIT extends BaseIT {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private AssetRepositoryPort assetRepositoryPort;

    @Test
    void upload_whenSaveFails_shouldTriggerGlobalExceptionHandler() {
        // given
        final AssetUploadRequestDto request = AssetUploadRequestDto.builder()
                .filename("test.txt")
                .encodedFile("encodedContent")
                .contentType("text/plain").build();

        when(assetRepositoryPort.save(any()))
                .thenReturn(Mono.error(new RuntimeException("DB connection failed")));

        webTestClient.post()
                .uri("/api/mgmt/1/assets/actions/upload")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(ErrorResponseDto.class)
                .value(response -> {
                    assertThat(response.code())
                            .isEqualTo(GenericErrorCodes.GENERIC_ERROR_BUG.getCode());
                    assertThat(response.message())
                            .contains("There was an unexpected error in the application");
                });
    }
}
