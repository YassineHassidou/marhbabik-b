package com.marhababik360.chat.controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.marhababik360.chat.config.JsonConfig;
import com.marhababik360.chat.dto.SendMessageRequest;
import com.marhababik360.chat.dto.UserContext;
import com.marhababik360.chat.exception.ApiException;
import com.marhababik360.chat.exception.ErrorResponse;
import com.marhababik360.chat.service.ChatService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ChatController {
    private final ChatService chatService;
    private final Gson gson = JsonConfig.gson();
    public ChatController(ChatService chatService) { this.chatService = chatService; }

    public void register(HttpServer server) {
        server.createContext("/messages", this::handleMessages);
        server.createContext("/health", exchange -> sendJson(exchange, 200, Map.of("status", "UP")));
    }

    private void handleMessages(HttpExchange exchange) throws IOException {
        withCors(exchange);
        if (handleOptions(exchange)) return;
        try {
            UserContext context = userContext(exchange);
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 201, chatService.send(context, gson.fromJson(readBody(exchange), SendMessageRequest.class)));
            } else if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String receiverId = query(exchange, "receiverId");
                sendJson(exchange, 200, chatService.conversation(context, receiverId));
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
        if (context.userId == null || context.role == null) throw new ApiException(401, "Unauthorized", "Authenticated user headers are required");
        return context;
    }

    private String query(HttpExchange exchange, String key) {
        String raw = exchange.getRequestURI().getRawQuery();
        if (raw == null) return null;
        for (String part : raw.split("&")) {
            String[] pair = part.split("=", 2);
            String k = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
            if (key.equals(k)) return pair.length == 2 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
        }
        return null;
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
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type,Authorization,X-User-Id,X-User-Role");
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
