package com.marhababik360.gateway.config;

import com.marhababik360.gateway.exception.ApiException;

public class RouteConfig {
    private final String auth = System.getenv().getOrDefault("AUTH_SERVICE_URL", "http://localhost:8081");
    private final String user = System.getenv().getOrDefault("USER_SERVICE_URL", "http://localhost:8082");
    private final String category = System.getenv().getOrDefault("CATEGORY_SERVICE_URL", "http://localhost:8083");
    private final String catalog = System.getenv().getOrDefault("SERVICE_CATALOG_URL", "http://localhost:8084");
    private final String chat = System.getenv().getOrDefault("CHAT_SERVICE_URL", "http://localhost:8085");

    public String targetBase(String path) {
        if (path.startsWith("/auth/")) return auth;
        if (path.startsWith("/users")) return user;
        if (path.startsWith("/categories")) return category;
        if (path.startsWith("/services")) return catalog;
        if (path.startsWith("/messages")) return chat;
        throw new ApiException(404, "Not Found", "No route for path");
    }
}
