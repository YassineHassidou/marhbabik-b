package com.marhababik360.category.controller;

import com.google.gson.Gson;
import com.marhababik360.category.config.JsonConfig;
import com.marhababik360.category.exception.ApiException;
import com.marhababik360.category.exception.ErrorResponse;
import com.marhababik360.category.service.CategoryService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class CategoryController {
    private final CategoryService categoryService;
    private final Gson gson = JsonConfig.gson();

    public CategoryController(CategoryService categoryService) { this.categoryService = categoryService; }

    public void register(HttpServer server) {
        server.createContext("/categories", this::handleCategories);
        server.createContext("/health", exchange -> sendJson(exchange, 200, Map.of("status", "UP")));
    }

    private void handleCategories(HttpExchange exchange) throws IOException {
        withCors(exchange);
        if (handleOptions(exchange)) return;
        try {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) throw new ApiException(405, "Method Not Allowed", "HTTP method not allowed");
            sendJson(exchange, 200, categoryService.list());
        } catch (Exception exception) {
            sendError(exchange, exception);
        }
    }

    private boolean handleOptions(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        return false;
    }

    private void withCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type,Authorization");
    }

    private void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        withCors(exchange);
        byte[] payload = gson.toJson(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, payload.length);
        exchange.getResponseBody().write(payload);
        exchange.close();
    }

    private void sendError(HttpExchange exchange, Exception exception) throws IOException {
        ApiException apiException = exception instanceof ApiException known ? known : new ApiException(500, "Internal Server Error", "Unexpected server error");
        if (!(exception instanceof ApiException)) exception.printStackTrace();
        sendJson(exchange, apiException.status(), ErrorResponse.of(apiException, exchange.getRequestURI().getPath()));
    }
}
