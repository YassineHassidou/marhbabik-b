package com.marhababik360.gateway.controller;

import com.google.gson.Gson;
import com.marhababik360.gateway.config.JsonConfig;
import com.marhababik360.gateway.exception.ApiException;
import com.marhababik360.gateway.exception.ErrorResponse;
import com.marhababik360.gateway.service.GatewayService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class GatewayController {
    private final GatewayService gatewayService;
    private final Gson gson = JsonConfig.gson();
    public GatewayController(GatewayService gatewayService) { this.gatewayService = gatewayService; }

    public void register(HttpServer server) {
        server.createContext("/", this::handle);
    }

    private void handle(HttpExchange exchange) throws IOException {
        withCors(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }
        try {
            if ("/health".equals(exchange.getRequestURI().getPath())) {
                sendJson(exchange, 200, Map.of("status", "UP"));
                return;
            }
            gatewayService.proxy(exchange);
        } catch (Exception exception) {
            sendError(exchange, exception);
        }
    }

    private void withCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type,Authorization");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "3600");
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
        ApiException apiException = exception instanceof ApiException known ? known : new ApiException(500, "Internal Server Error", "Gateway error");
        if (!(exception instanceof ApiException)) exception.printStackTrace();
        sendJson(exchange, apiException.status(), ErrorResponse.of(apiException, exchange.getRequestURI().getPath()));
    }
}
