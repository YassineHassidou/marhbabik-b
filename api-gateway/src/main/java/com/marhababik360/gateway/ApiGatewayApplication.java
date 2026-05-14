package com.marhababik360.gateway;

import com.marhababik360.gateway.config.RouteConfig;
import com.marhababik360.gateway.controller.GatewayController;
import com.marhababik360.gateway.service.GatewayService;
import com.marhababik360.gateway.service.JwtService;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Executors;

public class ApiGatewayApplication {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        GatewayService gatewayService = new GatewayService(client, new RouteConfig(), new JwtService());
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        new GatewayController(gatewayService).register(server);
        server.setExecutor(Executors.newFixedThreadPool(32));
        server.start();
        System.out.println("api-gateway started on port " + port);
    }
}
