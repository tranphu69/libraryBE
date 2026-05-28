package com.example.library.util;

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
}
