package com.example.library.util;

import com.example.library.exception.ErrorCode;

import java.text.MessageFormat;

public class DataUtils {
    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isNull(Long value) {
        return value == null;
    }

    public static boolean isNull(Integer value) {
        return value == null;
    }

    public static boolean maxLengthNotEmpty(String value, int maxLength) {
        return value != null && value.trim().length() > maxLength;
    }

    public static boolean maxLength(String value, int maxLength) {
        return value.trim().length() > maxLength;
    }

    public static String strConcatenation(ErrorCode errorCode, Object... args) {
        return MessageFormat.format(errorCode.getMessage(), args);
    }

    public static boolean isNumber(String value) {
        if(value == null || value.trim().isEmpty()) {
            return false;
        }
        try {
            Long.parseLong(value.trim());
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }
}
