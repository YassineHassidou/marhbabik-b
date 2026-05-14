package com.marhababik360.category.config;

import java.sql.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class DatabaseConfig {
    private final String url = "jdbc:sqlite:" + System.getenv().getOrDefault("CATEGORY_DB_PATH", "data/category-service.db");

    public Connection getConnection() throws SQLException { return DriverManager.getConnection(url); }

    public void initialize() throws SQLException {
        ensureParentDirectory();
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS categories (
                        id TEXT PRIMARY KEY,
                        name TEXT NOT NULL UNIQUE,
                        icon TEXT,
                        sort_order INTEGER NOT NULL
                    )
                    """);
        }
        seed();
    }

    private void ensureParentDirectory() {
        try {
            Path parent = Path.of(url.replace("jdbc:sqlite:", "")).toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not create database directory", exception);
        }
    }

    private void seed() throws SQLException {
        List<String[]> rows = List.of(
                new String[]{"transport", "Transport", "directions_car", "1"},
                new String[]{"food", "Food", "restaurant", "2"},
                new String[]{"housing", "Housing", "home", "3"},
                new String[]{"guide", "Guide", "map", "4"},
                new String[]{"events", "Events", "event", "5"},
                new String[]{"shopping", "Shopping", "shopping_bag", "6"},
                new String[]{"wellness", "Wellness", "spa", "7"},
                new String[]{"other", "Other", "category", "8"}
        );
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT OR IGNORE INTO categories(id, name, icon, sort_order) VALUES(?, ?, ?, ?)")) {
            for (String[] row : rows) {
                statement.setString(1, row[0]);
                statement.setString(2, row[1]);
                statement.setString(3, row[2]);
                statement.setInt(4, Integer.parseInt(row[3]));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }
}
