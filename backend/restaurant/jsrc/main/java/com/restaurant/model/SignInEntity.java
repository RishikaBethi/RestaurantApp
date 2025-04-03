package com.restaurant.model;

public class SignInEntity {
    private String email;
    private String password;

    public SignInEntity(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getter and Setter for email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Getter and Setter for password
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static SignInEntity fromJson(String jsonString) {
        JSONObject json = new JSONObject(jsonString);
        String email = json.optString("email", null);
        String password = json.optString("password", null);
        return new SignInEntity(email, password);
    }
}
