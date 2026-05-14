package com.marhababik360.category;

import com.marhababik360.category.config.DatabaseConfig;
import com.marhababik360.category.controller.CategoryController;
import com.marhababik360.category.repository.CategoryRepository;
import com.marhababik360.category.service.CategoryService;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class CategoryServiceApplication {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8083"));
        DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.initialize();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        new CategoryController(new CategoryService(new CategoryRepository(databaseConfig))).register(server);
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
        System.out.println("category-service started on port " + port);
    }
}
