package com.example.library.util;

import com.example.library.exception.ErrorCode;

import java.text.MessageFormat;
import java.util.Collection;

public class DataUtils {
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

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

    public static boolean isEmptyList(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches(EMAIL_REGEX);
    }

    public static boolean isNumber(String value) {
        if(value == null || value.trim().isEmpty()) {
            return true;
        }
        try {
            Long.parseLong(value.trim());
            return false;
        } catch(NumberFormatException e) {
            return true;
        }
    }
}
