package com.example.library.util;

import java.util.regex.Pattern;

public class SecurityUtils {
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])" +
                    "(?=.*[A-Z])" +
                    "(?=.*\\d)" +
                    "(?=.*[@$!%*?&^#()_+\\-=\\[\\]{};':\"\\\\|,.<>/?])" +
                    "[A-Za-z\\d@$!%*?&^#()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]{8,15}$"
    );

    public static boolean isValid(String password) {
        if (password == null) {
            return false;
        }
        return !PASSWORD_PATTERN.matcher(password).matches();
    }
}
