package com.restaurant.dtos;

import org.json.JSONObject;

import javax.validation.constraints.NotBlank;

public class SignInDTO {
    @NotBlank(message = "Email is mandatory")
    private String email;

    @NotBlank(message = "Password is mandatory")
    private String password;

    // No-args constructor
    public SignInDTO() {}

    // All-args constructor
    public SignInDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters and setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public static SignInDTO fromJson(String jsonString) {
        JSONObject json = new JSONObject(jsonString);
        String email = json.optString("email", null);
        String password = json.optString("password", null);
        return new SignInDTO(email, password);
    }
}