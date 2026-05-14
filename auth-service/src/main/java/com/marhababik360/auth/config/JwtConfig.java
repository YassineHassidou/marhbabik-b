package com.marhababik360.auth.config;

public final class JwtConfig {
    public static final String ISSUER = "marhababik360";

    private JwtConfig() {
    }

    public static String secret() {
        return System.getenv().getOrDefault("JWT_SECRET", "change-this-secret-in-production");
    }

    public static long expirationSeconds() {
        return Long.parseLong(System.getenv().getOrDefault("JWT_EXPIRATION_SECONDS", "86400"));
    }
}
