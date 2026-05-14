package com.marhababik360.catalog;

import com.marhababik360.catalog.config.DatabaseConfig;
import com.marhababik360.catalog.controller.ServiceController;
import com.marhababik360.catalog.repository.ServiceRepository;
import com.marhababik360.catalog.service.ServiceCatalogService;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ServiceCatalogApplication {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8084"));
        DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.initialize();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        new ServiceController(new ServiceCatalogService(new ServiceRepository(databaseConfig))).register(server);
        server.setExecutor(Executors.newFixedThreadPool(16));
        server.start();
        System.out.println("service-catalog-service started on port " + port);
    }
}
