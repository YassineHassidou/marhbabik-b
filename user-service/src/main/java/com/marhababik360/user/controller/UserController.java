package com.marhababik360.user.controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.marhababik360.user.config.JsonConfig;
import com.marhababik360.user.dto.UpdateUserRequest;
import com.marhababik360.user.dto.UserContext;
import com.marhababik360.user.exception.ApiException;
import com.marhababik360.user.exception.ErrorResponse;
import com.marhababik360.user.service.UserService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class UserController {
    private final UserService userService;
    private final Gson gson = JsonConfig.gson();

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public void register(HttpServer server) {
        server.createContext("/users/me", this::handleMe);
        server.createContext("/users/", this::handleById);
        server.createContext("/health", exchange -> sendJson(exchange, 200, Map.of("status", "UP")));
    }

    private void handleMe(HttpExchange exchange) throws IOException {
        withCors(exchange);
        if (handleOptions(exchange)) return;
        try {
            UserContext context = userContext(exchange);
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 200, userService.getOrCreateProfile(context));
            } else if ("PUT".equalsIgnoreCase(exchange.getRequestMethod()) || "PATCH".equalsIgnoreCase(exchange.getRequestMethod())) {
                UpdateUserRequest request = gson.fromJson(readBody(exchange), UpdateUserRequest.class);
                sendJson(exchange, 200, userService.updateProfile(context, request));
            } else {
                throw new ApiException(405, "Method Not Allowed", "HTTP method not allowed");
            }
        } catch (Exception exception) {
            sendError(exchange, exception);
        }
    }

    private void handleById(HttpExchange exchange) throws IOException {
        withCors(exchange);
        if (handleOptions(exchange)) return;
        try {
            requireMethod(exchange, "GET");
            UserContext context = userContext(exchange);
            String id = URLDecoder.decode(exchange.getRequestURI().getPath().substring("/users/".length()), StandardCharsets.UTF_8);
            sendJson(exchange, 200, userService.getById(context, id));
        } catch (Exception exception) {
            sendError(exchange, exception);
        }
    }

    private UserContext userContext(HttpExchange exchange) {
        UserContext context = new UserContext();
        context.userId = first(exchange, "X-User-Id");
        context.role = first(exchange, "X-User-Role");
        context.email = first(exchange, "X-User-Email");
        String fullName = first(exchange, "X-User-FullName");
        context.fullName = fullName == null ? null : URLDecoder.decode(fullName, StandardCharsets.UTF_8);
        if (context.userId == null || context.role == null) {
            throw new ApiException(401, "Unauthorized", "Authenticated user headers are required");
        }
        return context;
    }

    private String first(HttpExchange exchange, String header) {
        return exchange.getRequestHeaders().getFirst(header);
    }

    private void requireMethod(HttpExchange exchange, String method) {
        if (!method.equalsIgnoreCase(exchange.getRequestMethod())) throw new ApiException(405, "Method Not Allowed", "HTTP method not allowed");
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
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type,Authorization,X-User-Id,X-User-Role,X-User-Email,X-User-FullName");
    }

    private String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
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
        ApiException apiException;
        if (exception instanceof ApiException known) apiException = known;
        else if (exception instanceof JsonSyntaxException) apiException = new ApiException(400, "Bad Request", "Invalid JSON body");
        else {
            apiException = new ApiException(500, "Internal Server Error", "Unexpected server error");
            exception.printStackTrace();
        }
        sendJson(exchange, apiException.status(), ErrorResponse.of(apiException, exchange.getRequestURI().getPath()));
    }
}
