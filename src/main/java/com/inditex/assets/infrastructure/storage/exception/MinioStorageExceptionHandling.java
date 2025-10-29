package com.inditex.assets.infrastructure.storage.exception;

import io.minio.errors.ErrorResponseException;
import io.minio.errors.InternalException;
import io.minio.errors.ServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class MinioStorageExceptionHandling {

    public MinioStorageException mapToStorageException(final Throwable throwable) {

        if (throwable instanceof ErrorResponseException ex) {
            final String code = ex.errorResponse().code();
            final String message = ex.errorResponse().message();

            final HttpStatus status = switch (code) {
                case "NoSuchBucket" -> HttpStatus.NOT_FOUND;
                case "AccessDenied" -> HttpStatus.FORBIDDEN;
                case "InvalidAccessKeyId" -> HttpStatus.UNAUTHORIZED;
                case "EntityTooLarge" -> HttpStatus.PAYLOAD_TOO_LARGE;
                case "SlowDown" -> HttpStatus.TOO_MANY_REQUESTS;
                default -> HttpStatus.BAD_GATEWAY;
            };

            log.warn("MinIO error: code={}, message={}", code, message);
            return new MinioStorageException(status, ex.errorResponse().message(), message);
        }

        if (throwable instanceof ServerException || throwable instanceof InternalException) {
            log.warn("MinIO error: code={}, message={}", HttpStatus.BAD_GATEWAY, throwable.getMessage());
            return new MinioStorageException(HttpStatus.BAD_GATEWAY, throwable.getMessage(), "Internal error in MinIO server");
        }

        if (throwable instanceof IOException e) {
            log.warn("MinIO error: code={}, message={}", HttpStatus.BAD_GATEWAY, e.getMessage());
            return new MinioStorageException(HttpStatus.BAD_GATEWAY, e.getMessage(), "Network or file access error");
        }

        if (throwable instanceof IllegalArgumentException e) {
            log.warn("MinIO error: code={}, message={}", HttpStatus.BAD_REQUEST, e.getMessage());
            return new MinioStorageException(HttpStatus.BAD_REQUEST, e.getMessage(), "File format error");
        }

        // Unexpected error
        log.error("Unexpected error in MinIO: {}", throwable.getMessage(), throwable);
        return new MinioStorageException(HttpStatus.INTERNAL_SERVER_ERROR, throwable.getMessage(), "Internal error when uploading file");
    }

}
