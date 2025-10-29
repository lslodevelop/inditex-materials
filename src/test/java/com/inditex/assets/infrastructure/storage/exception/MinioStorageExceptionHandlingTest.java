package com.inditex.assets.infrastructure.storage.exception;

import io.minio.errors.ErrorResponseException;
import io.minio.errors.InternalException;
import io.minio.errors.ServerException;
import io.minio.messages.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MinioStorageExceptionHandlingTest {

    @InjectMocks
    private MinioStorageExceptionHandling minioStorageExceptionHandling;

    @Test
    void mapToStorageErrorResponseExceptionNotFoundTest() {
        //given
        final ErrorResponse errorResponse = mock(ErrorResponse.class);
        when(errorResponse.code()).thenReturn("NoSuchBucket");
        when(errorResponse.message()).thenReturn("Bucket not found");

        final ErrorResponseException errorResponseException = mock(ErrorResponseException.class);
        when(errorResponseException.errorResponse()).thenReturn(errorResponse);

        //when
        final MinioStorageException result = minioStorageExceptionHandling.mapToStorageException(errorResponseException);

        //then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getMessage()).contains("Bucket not found");
    }

    @Test
    void mapToStorageErrorResponseExceptionBadGatewayTest() {
        //given
        final ErrorResponse errorResponse = mock(ErrorResponse.class);
        when(errorResponse.code()).thenReturn("SomethingElse");
        when(errorResponse.message()).thenReturn("Unexpected");

        final ErrorResponseException errorResponseException = mock(ErrorResponseException.class);
        when(errorResponseException.errorResponse()).thenReturn(errorResponse);

        //when
        final MinioStorageException result = minioStorageExceptionHandling.mapToStorageException(errorResponseException);

        //then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(result.getMessage()).contains("Unexpected");
    }

    @Test
    void mapServerExceptionBadGatewayTest() {
        //given
        final ServerException serverException = new ServerException("Server crashed", 1, "message");

        //when
        final MinioStorageException result = minioStorageExceptionHandling.mapToStorageException(serverException);

        //then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(result.getMessage()).contains("Internal error in MinIO server");
    }

    @Test
    void mapInternalExceptionBadGatewayTest() {
        //given
        final InternalException internalException = new InternalException("Internal error", "httpTrace");

        //when
        final MinioStorageException result = minioStorageExceptionHandling.mapToStorageException(internalException);

        //then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(result.getMessage()).contains("Internal error");
    }

    @Test
    void mapIOExceptionBadGatewayTest() {
        //given
        final IOException ex = new IOException("Network error");

        //when
        final MinioStorageException result = minioStorageExceptionHandling.mapToStorageException(ex);

        //then
        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(result.getMessage()).contains("Network or file access error");
    }

    @Test
    void mapIllegalArgumentExceptionBadRequestTest() {
        //given
        final IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Invalid Base64");

        //when
        final MinioStorageException result = minioStorageExceptionHandling.mapToStorageException(illegalArgumentException);

        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getMessage()).contains("File format error");
    }

    @Test
    void mapUnexpectedExceptionInternalServerErrorTest() {
        //given
        final RuntimeException runtimeException = new RuntimeException("Unexpected error");

        //when
        final MinioStorageException result = minioStorageExceptionHandling.mapToStorageException(runtimeException);

        assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result.getMessage()).contains("Internal error when uploading file");
    }

}