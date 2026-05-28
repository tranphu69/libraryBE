package com.example.library.exception;

import lombok.Getter;

import java.text.MessageFormat;

@Getter
public enum ErrorCode {
    NOT_EMPTY("E01", "{0} không được để trống!"),
    NOT_LENGTH("E02", "{0} không được vượt quá {1} ký tự!"),
    NOT_VALID("E03", "Giá trị {0} không hợp lệ!"),
    NOT_DUPLICATE("E04", "{0} không được trùng nhau!"),
    NOT_EXIST("E05", "{0} không tồn tại!")
    ;
    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String formatMessage(Object... args) {
        return MessageFormat.format(this.message, args);
    }
}
