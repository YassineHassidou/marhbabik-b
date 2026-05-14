package com.marhababik360.catalog.controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.marhababik360.catalog.config.JsonConfig;
import com.marhababik360.catalog.dto.ServiceRequest;
import com.marhababik360.catalog.dto.UserContext;
import com.marhababik360.catalog.exception.ApiException;
import com.marhababik360.catalog.exception.ErrorResponse;
import com.marhababik360.catalog.service.ServiceCatalogService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ServiceController {
    private final ServiceCatalogService serviceCatalogService;
    private final Gson gson = JsonConfig.gson();

    public ServiceController(ServiceCatalogService serviceCatalogService) { this.serviceCatalogService = serviceCatalogService; }

    public void register(HttpServer server) {
        server.createContext("/services", this::handleCollection);
        server.createContext("/services/", this::handleItem);
        server.createContext("/health", exchange -> sendJson(exchange, 200, Map.of("status", "UP")));
    }

    private void handleCollection(HttpExchange exchange) throws IOException {
        withCors(exchange);
        if (handleOptions(exchange)) return;
        try {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 200, serviceCatalogService.list(query(exchange)));
            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 201, serviceCatalogService.create(userContext(exchange), gson.fromJson(readBody(exchange), ServiceRequest.class)));
            } else {
                throw new ApiException(405, "Method Not Allowed", "HTTP method not allowed");
            }
        } catch (Exception exception) {
            sendError(exchange, exception);
        }
    }

    private void handleItem(HttpExchange exchange) throws IOException {
        withCors(exchange);
        if (handleOptions(exchange)) return;
        try {
            String id = URLDecoder.decode(exchange.getRequestURI().getPath().substring("/services/".length()), StandardCharsets.UTF_8);
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                sendJson(exchange, 200, serviceCatalogService.get(id));
            } else if ("PUT".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method)) {
                sendJson(exchange, 200, serviceCatalogService.update(userContext(exchange), id, gson.fromJson(readBody(exchange), ServiceRequest.class)));
            } else if ("DELETE".equalsIgnoreCase(method)) {
                serviceCatalogService.delete(userContext(exchange), id);
                sendJson(exchange, 200, Map.of("deleted", true));
            } else {
                throw new ApiException(405, "Method Not Allowed", "HTTP method not allowed");
            }
        } catch (Exception exception) {
            sendError(exchange, exception);
        }
    }

    private UserContext userContext(HttpExchange exchange) {
        UserContext context = new UserContext();
        context.userId = exchange.getRequestHeaders().getFirst("X-User-Id");
        context.role = exchange.getRequestHeaders().getFirst("X-User-Role");
        String fullName = exchange.getRequestHeaders().getFirst("X-User-FullName");
        context.fullName = fullName == null ? null : URLDecoder.decode(fullName, StandardCharsets.UTF_8);
        if (context.userId == null || context.role == null) throw new ApiException(401, "Unauthorized", "Authenticated user headers are required");
        return context;
    }

    private Map<String, String> query(HttpExchange exchange) {
        Map<String, String> query = new HashMap<>();
        String raw = exchange.getRequestURI().getRawQuery();
        if (raw == null || raw.isBlank()) return query;
        for (String part : raw.split("&")) {
            String[] pair = part.split("=", 2);
            String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
            String value = pair.length == 2 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
            query.put(key, value);
        }
        return query;
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
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type,Authorization,X-User-Id,X-User-Role,X-User-FullName");
    }

    private String readBody(HttpExchange exchange) throws IOException { return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8); }
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
        else { apiException = new ApiException(500, "Internal Server Error", "Unexpected server error"); exception.printStackTrace(); }
        sendJson(exchange, apiException.status(), ErrorResponse.of(apiException, exchange.getRequestURI().getPath()));
    }
}
