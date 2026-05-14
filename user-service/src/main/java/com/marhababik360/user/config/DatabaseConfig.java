package com.marhababik360.user.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Path;

public class DatabaseConfig {
    private final String url = "jdbc:sqlite:" + System.getenv().getOrDefault("USER_DB_PATH", "data/user-service.db");

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    public void initialize() throws SQLException {
        ensureParentDirectory();
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id TEXT PRIMARY KEY,
                        full_name TEXT NOT NULL,
                        email TEXT,
                        role TEXT NOT NULL CHECK(role IN ('visitor', 'worker')),
                        age INTEGER,
                        cn TEXT,
                        phone TEXT,
                        category TEXT,
                        business_name TEXT,
                        profile_photo TEXT,
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
