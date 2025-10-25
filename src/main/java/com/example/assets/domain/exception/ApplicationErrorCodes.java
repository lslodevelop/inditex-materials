package com.example.assets.domain.exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ApplicationErrorCodes implements BaseErrorCode {
    NOT_FOUND_ASSET_ERROR(Prefix.APPLICATION + "01"),
    INVALID_LOCAL_DATE_TIME(Prefix.APPLICATION + "02"),
    INVALID_URL(Prefix.APPLICATION + "03"),
    MALFORMED_URL(Prefix.APPLICATION + "04"),
    WRONG_PATH(Prefix.APPLICATION + "05"),
    WRONG_BODY(Prefix.APPLICATION + "06"),
    ;

    private final String errorCode;

    @Override
    public String getCode() {
        return this.errorCode;
    }

    private static final class Prefix {
        private static final String APPLICATION = "error.assets.application.";
    }
}
