package com.example.library.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserConstant {
    public static final String CODE = "Mã người dùng";
    public static final String CODE_LENGTH = "100";
    public static final String COMMON_LENGTH = "255";
    public static final String FULL_NAME = "Họ tên";
    public static final String PASSWORD = "Mật khẩu";
    public static final String PASSWORD_LENGTH = "15";
    public static final String LIST_ROLE = "Danh sách vai trò";
    public static final String NOT_EXIST_ROLE = "Có vai trò trong danh sách vai trò";
    public static final String USER = "Người dùng";

    public static final Long ACTIVE = 1L;
    public static final Long INACTIVE = 0L;
    public static final Long DELETED = -1L;

    public static final String UPDATED_AT = "updatedAt";
    public static final String DESC = "DESC";

    public static final int MAX_LENGTH_CODE = 100;
    public static final int MAX_LENGTH = 255;
    public static final int MAX_LENGTH_PASSWORD = 15;
    public static final int MIN_LENGTH_PASSWORD = 8;

    public static final long MAX_FILE_SIZE = 1024L * 1024;
}
