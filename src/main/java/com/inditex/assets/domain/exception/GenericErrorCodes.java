package com.inditex.assets.domain.exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum GenericErrorCodes implements BaseErrorCode {

    GENERIC_ERROR_BUG(Prefix.GENERIC + "00");

    private final String errorCode;

    @Override
    public String getCode() {
        return this.errorCode;
    }

    private static final class Prefix {
        static final String GENERIC = "error.assets.generic.";
    }
}
