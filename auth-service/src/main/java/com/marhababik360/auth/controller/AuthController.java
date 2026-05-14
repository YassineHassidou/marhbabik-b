package com.marhababik360.auth.controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.marhababik360.auth.config.JsonConfig;
import com.marhababik360.auth.dto.LoginRequest;
import com.marhababik360.auth.dto.RegisterRequest;
import com.marhababik360.auth.exception.ApiException;
import com.marhababik360.auth.exception.ErrorResponse;
import com.marhababik360.auth.service.AuthService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AuthController {
    private final AuthService authService;
    private final Gson gson = JsonConfig.gson();

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public void register(HttpServer server) {
        server.createContext("/auth/register", this::handleRegister);
        server.createContext("/auth/login", this::handleLogin);
        server.createContext("/health", this::handleHealth);
    }

    private void handleRegister(HttpExchange exchange) throws IOException {
        withCors(exchange);
        if (handleOptions(exchange)) return;
        try {
            requireMethod(exchange, "POST");
            RegisterRequest request = gson.fromJson(readBody(exchange), RegisterRequest.class);
            sendJson(exchange, 201, authService.register(request));
        } catch (Exception exception) {
            sendError(exchange, exception);
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        withCors(exchange);
        if (handleOptions(exchange)) return;
        try {
            requireMethod(exchange, "POST");
            LoginRequest request = gson.fromJson(readBody(exchange), LoginRequest.class);
            sendJson(exchange, 200, authService.login(request));
        } catch (Exception exception) {
            sendError(exchange, exception);
        }
    }

    private void handleHealth(HttpExchange exchange) throws IOException {
        withCors(exchange);
        if (handleOptions(exchange)) return;
        sendJson(exchange, 200, java.util.Map.of("status", "UP"));
    }

    private void requireMethod(HttpExchange exchange, String method) {
        if (!method.equalsIgnoreCase(exchange.getRequestMethod())) {
            throw new ApiException(405, "Method Not Allowed", "HTTP method not allowed");
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
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type,Authorization,X-User-Id,X-User-Role,X-User-Email");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "3600");
    }

    private String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] payload = gson.toJson(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, payload.length);
        exchange.getResponseBody().write(payload);
        exchange.close();
    }

    private void sendError(HttpExchange exchange, Exception exception) throws IOException {
        ApiException apiException;
        if (exception instanceof ApiException known) {
            apiException = known;
        } else if (exception instanceof JsonSyntaxException) {
            apiException = new ApiException(400, "Bad Request", "Invalid JSON body");
        } else {
            apiException = new ApiException(500, "Internal Server Error", "Unexpected server error");
            exception.printStackTrace();
        }
        sendJson(exchange, apiException.status(), ErrorResponse.of(apiException, exchange.getRequestURI().getPath()));
    }
}
