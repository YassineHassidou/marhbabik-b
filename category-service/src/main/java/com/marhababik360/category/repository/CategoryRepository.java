package com.marhababik360.category.repository;

import com.marhababik360.category.config.DatabaseConfig;
import com.marhababik360.category.exception.ApiException;
import com.marhababik360.category.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryRepository {
    private final DatabaseConfig databaseConfig;
    public CategoryRepository(DatabaseConfig databaseConfig) { this.databaseConfig = databaseConfig; }

    public List<Category> findAll() {
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM categories ORDER BY sort_order ASC");
             ResultSet rs = statement.executeQuery()) {
            List<Category> categories = new ArrayList<>();
            while (rs.next()) {
                Category category = new Category();
                category.id = rs.getString("id");
                category.name = rs.getString("name");
                category.icon = rs.getString("icon");
                category.sortOrder = rs.getInt("sort_order");
                categories.add(category);
            }
            return categories;
        } catch (SQLException exception) {
            throw new ApiException(500, "Internal Server Error", "Could not read categories");
        }
    }
}
