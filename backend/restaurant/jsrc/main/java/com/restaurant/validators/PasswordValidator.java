package com.restaurant.validators;

import java.util.logging.Logger;

public class PasswordValidator {
    private static final Logger logger = Logger.getLogger(PasswordValidator.class.getName());
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 16;

    public static boolean validatePassword(String password) {
        if (password == null || password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            logger.info("Validation failed - Length or null check for: " + password);
            return false;
        }
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*].*");

        boolean result = hasUppercase && hasLowercase && hasDigit && hasSpecial;
        logger.info("Validation result: " + result + " for: " + password);
        return result;
    }
}