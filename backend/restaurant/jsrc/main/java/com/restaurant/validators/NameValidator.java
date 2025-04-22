package com.restaurant.validators;

import java.util.regex.Pattern;

public class NameValidator {
    // First name: starts with a letter, followed by letters, digits, or special characters (no spaces), up to 50 characters
    private static final String FIRST_NAME_REGEX = "^[A-Za-z][A-Za-z0-9@#%&*_.-]{0,49}$";

    // Last name: up to 50 characters, any letters, digits, or special characters (no spaces)
    private static final String LAST_NAME_REGEX = "^[A-Za-z0-9@#%&*_.-]{1,50}$";

    private static final Pattern FIRST_NAME_PATTERN = Pattern.compile(FIRST_NAME_REGEX);
    private static final Pattern LAST_NAME_PATTERN = Pattern.compile(LAST_NAME_REGEX);

    public static boolean validateFirstName(String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            return false;
        }
        return FIRST_NAME_PATTERN.matcher(firstName.trim()).matches();
    }

    public static boolean validateLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            return false;
        }
        return LAST_NAME_PATTERN.matcher(lastName.trim()).matches();
    }
}
