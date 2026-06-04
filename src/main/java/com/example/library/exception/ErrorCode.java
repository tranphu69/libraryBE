package com.example.library.exception;

import lombok.Getter;

import java.text.MessageFormat;

@Getter
public enum ErrorCode {
    NOT_EMPTY("E01", "{0} không được để trống"),
    NOT_LENGTH("E02", "{0} không được vượt quá {1} ký tự"),
    NOT_VALID("E03", "Giá trị {0} không hợp lệ"),
    NOT_DUPLICATE("E04", "{0} không được trùng nhau"),
    NOT_EXIST("E05", "{0} không tồn tại"),
    CODE_CHARACTER("E06", "{0} chỉ được chứa chữ, số và dấu '_'"),
    NOT_FILE("E07", "Tệp không được để trống"),
    OVER_CAPACITY("E08", "Vui lòng tải lên tệp nhỏ hơn {0}MB"),
    NOT_FORMAT_FILE("E09", "Tệp không đúng định dạng"),
    FILE_READ_ERROR("E10", "Tệp đang lỗi không thể mở được"),
    CHECK_TEMPLATE("E11", "Tệp hiện tại đang không giống với tệp mẫu vui lòng tải lại"),
    NOT_DELETE("E12", "{0} đang được sử dụng không thể xóa")
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
