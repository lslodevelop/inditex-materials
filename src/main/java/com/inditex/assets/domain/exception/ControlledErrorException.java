package com.inditex.assets.domain.exception;

import org.springframework.http.HttpStatus;

public class ControlledErrorException extends BaseException {

    public ControlledErrorException(final BaseErrorCode errorCode, final String message, HttpStatus httpStatus) {
        super(errorCode, message, httpStatus);
    }

    public ControlledErrorException(final BaseErrorCode errorCode, final String message) {
        super(errorCode, message);
    }

}
