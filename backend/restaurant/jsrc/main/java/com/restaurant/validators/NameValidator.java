package com.restaurant.validators;

import java.util.regex.Pattern;

public class NameValidator {
    private static final String REGEX = "^[A-Za-z]{1,50}$"; // Letters only, 1-50 chars
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    public static boolean validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return PATTERN.matcher(name.trim()).matches();
    }
}