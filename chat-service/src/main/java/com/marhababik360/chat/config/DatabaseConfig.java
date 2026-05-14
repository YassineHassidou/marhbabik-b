package com.marhababik360.chat.config;

import java.sql.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class DatabaseConfig {
    private final String url = "jdbc:sqlite:" + System.getenv().getOrDefault("CHAT_DB_PATH", "data/chat-service.db");
    public Connection getConnection() throws SQLException { return DriverManager.getConnection(url); }
    public void initialize() throws SQLException {
        ensureParentDirectory();
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS messages (
                        id TEXT PRIMARY KEY,
                        sender_id TEXT NOT NULL,
                        receiver_id TEXT NOT NULL,
                        text TEXT NOT NULL,
                        timestamp TEXT NOT NULL
                    )
                    """);
            statement.execute("CREATE INDEX IF NOT EXISTS idx_messages_users ON messages(sender_id, receiver_id, timestamp)");
        }
    }

    private void ensureParentDirectory() {
        try {
            Path parent = Path.of(url.replace("jdbc:sqlite:", "")).toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not create database directory", exception);
        }
    }
}
