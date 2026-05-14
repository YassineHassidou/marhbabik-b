package com.marhababik360.gateway.config;

public final class JwtConfig {
    public static final String ISSUER = "marhababik360";
    private JwtConfig() {}
    public static String secret() { return System.getenv().getOrDefault("JWT_SECRET", "change-this-secret-in-production"); }
}
