package com.marhababik360.chat;

import com.marhababik360.chat.config.DatabaseConfig;
import com.marhababik360.chat.controller.ChatController;
import com.marhababik360.chat.repository.MessageRepository;
import com.marhababik360.chat.service.ChatService;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ChatServiceApplication {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8085"));
        DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.initialize();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        new ChatController(new ChatService(new MessageRepository(databaseConfig))).register(server);
        server.setExecutor(Executors.newFixedThreadPool(16));
        server.start();
        System.out.println("chat-service started on port " + port);
    }
}
