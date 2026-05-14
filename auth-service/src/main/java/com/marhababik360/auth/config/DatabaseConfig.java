package com.marhababik360.auth.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Path;

public class DatabaseConfig {
    private final String url;

    public DatabaseConfig() {
        String dbPath = System.getenv().getOrDefault("AUTH_DB_PATH", "data/auth-service.db");
        this.url = "jdbc:sqlite:" + dbPath;
    }

    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(url);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    public void initialize() throws SQLException {
        ensureParentDirectory();
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id TEXT PRIMARY KEY,
                        full_name TEXT NOT NULL,
                        email TEXT NOT NULL UNIQUE,
                        password_hash TEXT NOT NULL,
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
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Could not create database directory", exception);
        }
    }
}
