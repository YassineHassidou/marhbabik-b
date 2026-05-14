package com.marhababik360.catalog.config;

import java.sql.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class DatabaseConfig {
    private final String url = "jdbc:sqlite:" + System.getenv().getOrDefault("CATALOG_DB_PATH", "data/service-catalog-service.db");
    public Connection getConnection() throws SQLException { return DriverManager.getConnection(url); }
    public void initialize() throws SQLException {
        ensureParentDirectory();
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS services (
                        id TEXT PRIMARY KEY,
                        title TEXT NOT NULL,
                        category TEXT NOT NULL,
                        description TEXT NOT NULL,
                        price REAL NOT NULL,
                        images TEXT NOT NULL,
                        location TEXT,
                        worker_id TEXT NOT NULL,
                        worker_name TEXT NOT NULL,
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL
                    )
                    """);
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
