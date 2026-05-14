package com.marhababik360.auth.dto;

public class AuthResponse {
    public String tokenType = "Bearer";
    public String accessToken;
    public long expiresIn;
    public UserResponse user;
}
