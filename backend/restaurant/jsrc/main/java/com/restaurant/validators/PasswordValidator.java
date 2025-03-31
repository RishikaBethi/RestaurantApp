package com.restaurant.validators;

public class PasswordValidator {
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 16;

    public static boolean validatePassword(String password) {
        if (password == null || password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            return false;
        }
        return password.matches(".*[A-Z].*") && // Uppercase
                password.matches(".*[0-9].*") && // Number
                password.matches(".*[!@#$%^&*()_+-=\\[\\]{};':\",.<>/?].*"); // Special character
    }
}