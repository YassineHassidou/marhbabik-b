package com.marhababik360.auth;

import com.marhababik360.auth.config.DatabaseConfig;
import com.marhababik360.auth.controller.AuthController;
import com.marhababik360.auth.repository.UserRepository;
import com.marhababik360.auth.service.AuthService;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class AuthServiceApplication {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8081"));
        DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.initialize();

        UserRepository userRepository = new UserRepository(databaseConfig);
        AuthService authService = new AuthService(userRepository);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        new AuthController(authService).register(server);
        server.setExecutor(Executors.newFixedThreadPool(16));
        server.start();
        System.out.println("auth-service started on port " + port);
    }
}
