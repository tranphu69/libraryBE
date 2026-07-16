package com.example.library.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

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
    NOT_DELETE("E12", "{0} đang được sử dụng không thể xóa"),
    AUTHENTICATION_ACCOUNT("E13", "Tài khoản hoặc mật khẩu không chính xác"),
    AUTHENTICATION_TOKEN("E14", "Token không hợp lệ"),
    NOT_TOKEN("E15", "Token không được để trống"),
    FAILED_EMAIL("E16", "Gửi email thất bại"),
    MFA_CHALLENGE_INVALID("E17", "Yêu cầu xác thực đa yếu tố (MFA) không hợp lệ hoặc đã hết hạn"),
    MFA_TOO_MANY_ATTEMPTS("E18", "Quá nhiều lần nhập mã OTP không hợp lệ. Vui lòng đăng nhập lại"),
    MFA_OTP_INVALID("E19", "Mã OTP bạn đã nhập không chính xác"),
    NOT_PASSWORD("E20", "Mật khẩu phải bao gồm ít nhất 1 chữ cái viết hoa, 1 chữ cái viết thường, 1 chữ số, 1 ký tự đặc biệt và có độ dài từ 8 đến 15 ký tự"),
    MAX_LEVEL("E21", "{0} thì cây chỉ được tối đa {1} cấp"),
    NOT_DUPLICATE_TYPE("E22", "{0} không được trùng nhau với cùng {1}"),

    ERROR_SYSTEM("1000", "Lỗi hệ thống, vui lòng thử lại sau", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHENTICATED_TOKEN("1001", "Token is invalid. Error: {0}"),
    UNAUTHORIZED("1002", "Bạn không có quyền truy cập tài nguyên này", HttpStatus.FORBIDDEN),
    UNAUTHENTICATED("1003", "Bạn chưa đăng nhập hoặc token không hợp lệ", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED("1004", "Tài khoản đã bị tạm khóa do nhập sai mật khẩu quá nhiều lần. Vui lòng thử lại sau 15 phút.", HttpStatus.FORBIDDEN),
    LIMIT_FILE("1005", "File vượt quá kích thước cho phép.", HttpStatus.PAYLOAD_TOO_LARGE),
    ;
    private final String code;
    private final String message;
    private HttpStatusCode statusCode;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    ErrorCode(String code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    public String formatMessage(Object... args) {
        return MessageFormat.format(this.message, args);
    }
}
