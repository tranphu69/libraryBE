package com.example.library.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PermissionConstant {
    public static final String CODE = "Mã quyền";
    public static final String CODE_LENGTH = "100";
    public static final String NAME = "Tên quyền";
    public static final String NAME_LENGTH = "255";
    public static final String DESCRIPTION = "Mô tả";
    public static final String DESCRIPTION_LENGTH = "1000";
    public static final String STATUS = "Trạng thái";
    public static final String PERMISSION = "Quyền";

    public static final Long ACTIVE = 1L;
    public static final Long INACTIVE = 0L;
    public static final Long DELETED = -1L;

    public static final String UPDATED_AT = "updatedAt";
    public static final String DESC = "DESC";

    public static final int MAX_LENGTH_CODE = 100;
    public static final int MAX_LENGTH_NAME = 255;
    public static final int MAX_LENGTH_DESCRIPTION = 1000;

    public static final long MAX_FILE_SIZE = 1024L * 1024;
}
