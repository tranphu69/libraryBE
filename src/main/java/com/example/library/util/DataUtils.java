package com.example.library.util;

import com.example.library.exception.ErrorCode;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Collection;

public class DataUtils {
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    public static LocalDate parseDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(date.trim(), DATE_FORMATTER);
    }

    public static boolean isValidDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return true;
        }
        try {
            LocalDate.parse(date.trim(), DATE_FORMATTER);
            return false;
        } catch (DateTimeParseException e) {
            return true;
        }
    }

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

    public static boolean passwordLength(String value, int maxLength, int minLength) {
        return value.trim().length() < minLength || value.trim().length() > maxLength;
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
