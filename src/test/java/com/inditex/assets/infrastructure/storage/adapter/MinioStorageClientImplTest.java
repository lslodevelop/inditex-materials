package com.inditex.assets.infrastructure.storage.adapter;

import com.inditex.assets.domain.model.Asset;
import com.inditex.assets.infrastructure.storage.config.StorageProperties;
import com.inditex.assets.infrastructure.storage.exception.MinioStorageException;
import com.inditex.assets.infrastructure.storage.exception.MinioStorageExceptionHandling;
import com.inditex.assets.infrastructure.storage.model.StorageMetadata;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MinioStorageClientImplTest {

    @InjectMocks
    private MinioStorageClientImpl minioStorageClient;

    @Mock
    private MinioClient minioClient;

    @Mock
    private StorageProperties storageProperties;

    @Mock
    private MinioStorageExceptionHandling minioStorageExceptionHandling;

    @Test
    void getFileTest() throws Exception {
        // given
        final byte[] decodedFile = "sample-content".getBytes(StandardCharsets.UTF_8);
        final String encodedFile = Base64.getEncoder().encodeToString(decodedFile);

        final GetObjectResponse mockResponse = mock(GetObjectResponse.class);
        final ArgumentCaptor<GetObjectArgs> getObjectArgsCaptor = ArgumentCaptor.forClass(GetObjectArgs.class);
        when(storageProperties.getBucket()).thenReturn("test-bucket");
        when(mockResponse.readAllBytes()).thenReturn(decodedFile);
        when(minioClient.getObject(getObjectArgsCaptor.capture())).thenReturn(mockResponse);

        // when
        final Mono<String> result = minioStorageClient.getFile("test.txt");

        // then
        StepVerifier.create(result)
                .expectNext(encodedFile)
                .verifyComplete();

        assertThat(getObjectArgsCaptor.getValue().bucket()).isEqualTo("test-bucket");
        assertThat(getObjectArgsCaptor.getValue().object()).isEqualTo("test.txt");
        verify(storageProperties).getBucket();
        verify(minioClient).getObject(any(GetObjectArgs.class));
    }

    @Test
    void uploadTest() throws Exception {
        // given
        final Asset asset = Asset.builder()
                .id("123")
                .filename("file.txt")
                .contentType("text/plain")
                .build();

        final byte[] bytes = "test".getBytes(StandardCharsets.UTF_8);
        final String encoded = Base64.getEncoder().encodeToString(bytes);
        final ObjectWriteResponse objectWriteResponse = mock(ObjectWriteResponse.class);
        final ArgumentCaptor<PutObjectArgs> putObjectArgsArgumentCaptor = ArgumentCaptor.forClass(PutObjectArgs.class);

        when(storageProperties.getBucket()).thenReturn("test-bucket");
        when(storageProperties.getEndpoint()).thenReturn("http://localhost:9000");
        when(minioClient.putObject(putObjectArgsArgumentCaptor.capture())).thenReturn(objectWriteResponse);

        // when
        final Mono<StorageMetadata> result = minioStorageClient.upload(asset, encoded);

        // then
        StepVerifier.create(result)
                .assertNext(metadata -> {
                    assertThat(metadata.getUrl()).isEqualTo("http://localhost:9000/test-bucket/file.txt");
                    assertThat(metadata.getSize()).isEqualTo(bytes.length);
                    assertThat(metadata.getUploadedAt()).isNotNull();
                })
                .verifyComplete();

        assertThat(putObjectArgsArgumentCaptor.getValue().bucket()).isEqualTo("test-bucket");
        assertThat(putObjectArgsArgumentCaptor.getValue().object()).isEqualTo("file.txt");
        assertThat(putObjectArgsArgumentCaptor.getValue().contentType()).isEqualTo("text/plain");
        verify(storageProperties, times(2)).getBucket();
        verify(storageProperties).getEndpoint();
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void uploadExceptionTest() throws Exception {
        // given
        final Asset asset = Asset.builder()
                .filename("file.txt")
                .contentType("text/plain")
                .build();

        final String encoded = Base64.getEncoder().encodeToString("wrong".getBytes());
        final RuntimeException runtimeException = new RuntimeException("Upload failed");
        final MinioStorageException minioStorageException = new MinioStorageException(HttpStatus.BAD_REQUEST, "Upload failed");

        when(storageProperties.getBucket()).thenReturn("test-bucket");
        doThrow(runtimeException)
                .when(minioClient)
                .putObject(any(PutObjectArgs.class));

        when(minioStorageExceptionHandling.mapToStorageException(runtimeException))
                .thenThrow(minioStorageException);

        // when
        final Mono<StorageMetadata> result = minioStorageClient.upload(asset, encoded);

        // then
        StepVerifier.create(result)
                .expectErrorMatches(e -> e == minioStorageException)
                .verify();

        verify(storageProperties).getBucket();
        verifyNoMoreInteractions(storageProperties);
        verify(minioStorageExceptionHandling).mapToStorageException(runtimeException);
    }

}