package com.inditex.assets.domain.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@EqualsAndHashCode(callSuper = true)
@Data
public class BaseException extends RuntimeException{

    private final transient BaseErrorCode errorCode;
    private final HttpStatus httpStatus;

    public BaseException(final BaseErrorCode baseErrorCode, final String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = baseErrorCode;
        this.httpStatus = httpStatus;
    }

    public BaseException(final BaseErrorCode baseErrorCode, final String message) {
        super(message);
        this.errorCode = baseErrorCode;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

}
