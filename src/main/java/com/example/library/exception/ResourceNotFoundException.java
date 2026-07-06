package com.example.library.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String formattedMessage;

    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.formattedMessage = errorCode.getMessage();
    }

    public ResourceNotFoundException(ErrorCode errorCode, Object... args) {
        super(errorCode.formatMessage(args));
        this.errorCode = errorCode;
        this.formattedMessage = errorCode.formatMessage(args);
    }
}
