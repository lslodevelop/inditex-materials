package com.inditex.assets.infrastructure.storage.adapter;

import com.inditex.assets.domain.model.Asset;
import com.inditex.assets.infrastructure.storage.config.LocalStorageProperties;
import com.inditex.assets.infrastructure.storage.model.StorageMetadata;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Base64;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class LocalStorageClientImplTest {

    private LocalStorageClientImpl localStorageClientImpl;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("uploads_test_");
        LocalStorageProperties props = new LocalStorageProperties(tempDir.toString());
        localStorageClientImpl = new LocalStorageClientImpl(props);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Delete tmp file
        Files.walk(tempDir)
                .sorted(Comparator.reverseOrder()) // first delete files, then folders
                .forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException ignored) {
                    }
                });
    }

    @Test
    void upload_shouldWriteDecodedFile_andReturnMetadata() {
        // given
        final Asset asset = Asset.builder().filename("test.txt").build();
        final String content = "Content test";
        final String encodedFile = Base64.getEncoder().encodeToString(content.getBytes());

        // when
        final Mono<StorageMetadata> result = localStorageClientImpl.upload(asset, encodedFile);

        // then
        StepVerifier.create(result)
                .assertNext(meta -> {
                    assertThat(meta.getUrl()).contains("test.txt");
                    assertThat(meta.getSize()).isEqualTo(content.length());
                    assertThat(meta.getUploadedAt()).isBeforeOrEqualTo(Instant.now());

                    // comprobar que el fichero existe físicamente
                    Path written = tempDir.resolve("test.txt");
                    assertThat(Files.exists(written)).isTrue();

                    try {
                        String saved = Files.readString(written);
                        assertThat(saved).isEqualTo(content);
                    } catch (IOException e) {
                        Assertions.fail("Error reading saved file", e);
                    }
                })
                .verifyComplete();
    }

    @Test
    void upload_shouldThrowError_whenInvalidBase64() {
        // given
        final Asset asset = Asset.builder().filename("broken.txt").build();
        final String invalidEncoded = "###@@@invalidBase64";

        // when
        final Mono<StorageMetadata> result = localStorageClientImpl.upload(asset, invalidEncoded);

        // then
        StepVerifier.create(result)
                .expectErrorMatches(err ->
                        err instanceof RuntimeException &&
                                err.getMessage().contains("Invalid Base64 input"))
                .verify();
    }

    @Test
    void getFile_shouldReturnBase64EncodedFileContent() throws IOException {
        // given
        final String content = "Content test";
        final Path filePath = tempDir.resolve("file.txt");
        Files.writeString(filePath, content);
        final String expectedEncoded = Base64.getEncoder().encodeToString(content.getBytes());

        // when
        Mono<String> result = localStorageClientImpl.getFile("file.txt");

        // then
        StepVerifier.create(result)
                .expectNext(expectedEncoded)
                .verifyComplete();
    }

    @Test
    void getFile_shouldThrowError_whenFileNotFound() {
        // given
        final String missingFile = "nope.txt";

        // when
        final Mono<String> result = localStorageClientImpl.getFile(missingFile);

        // then
        StepVerifier.create(result)
                .expectErrorMatches(err ->
                        err instanceof NoSuchFileException &&
                                err.getMessage().contains(missingFile))
                .verify();
    }


}