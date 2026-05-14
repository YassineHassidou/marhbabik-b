package com.marhababik360.user;

import com.marhababik360.user.config.DatabaseConfig;
import com.marhababik360.user.controller.UserController;
import com.marhababik360.user.repository.UserRepository;
import com.marhababik360.user.service.UserService;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class UserServiceApplication {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8082"));
        DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.initialize();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        new UserController(new UserService(new UserRepository(databaseConfig))).register(server);
        server.setExecutor(Executors.newFixedThreadPool(16));
        server.start();
        System.out.println("user-service started on port " + port);
    }
}
