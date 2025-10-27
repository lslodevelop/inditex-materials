package com.inditex.assets.infrastructure.storage.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class MinioStorageException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String responseBody;

    public MinioStorageException(final HttpStatus httpStatus, final String responseBody, final String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.responseBody = responseBody;
    }

}
