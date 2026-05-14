package com.marhababik360.gateway.service;

import com.marhababik360.gateway.config.RouteConfig;
import com.marhababik360.gateway.dto.UserClaims;
import com.marhababik360.gateway.exception.ApiException;
import com.marhababik360.gateway.mapper.HeaderMapper;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class GatewayService {
    private static final Set<String> HOP_BY_HOP = Set.of("connection", "host", "content-length", "transfer-encoding", "upgrade");
    private final HttpClient httpClient;
    private final RouteConfig routeConfig;
    private final JwtService jwtService;

    public GatewayService(HttpClient httpClient, RouteConfig routeConfig, JwtService jwtService) {
        this.httpClient = httpClient;
        this.routeConfig = routeConfig;
        this.jwtService = jwtService;
    }

    public void proxy(HttpExchange exchange) throws IOException, InterruptedException {
        String path = exchange.getRequestURI().getRawPath();
        String query = exchange.getRequestURI().getRawQuery();
        String target = routeConfig.targetBase(path) + path + (query == null ? "" : "?" + query);
        UserClaims claims = null;
        if (requiresAuth(path)) {
            claims = jwtService.verify(exchange);
        }

        byte[] requestBody = exchange.getRequestBody().readAllBytes();
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(target))
                .method(exchange.getRequestMethod(), HttpRequest.BodyPublishers.ofByteArray(requestBody));
        exchange.getRequestHeaders().forEach((name, values) -> {
            if (!HOP_BY_HOP.contains(name.toLowerCase()) && !"authorization".equalsIgnoreCase(name)) {
                for (String value : values) builder.header(name, value);
            }
        });
        if (claims != null) HeaderMapper.addUserHeaders(builder, claims);
        HttpResponse<byte[]> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
        response.headers().map().forEach((name, values) -> {
            if (!HOP_BY_HOP.contains(name.toLowerCase())) values.forEach(value -> exchange.getResponseHeaders().add(name, value));
        });
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(response.statusCode(), response.body().length);
        exchange.getResponseBody().write(response.body());
        exchange.close();
    }

    private boolean requiresAuth(String path) {
        return !(path.startsWith("/auth/"));
    }
}
