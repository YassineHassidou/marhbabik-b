package com.marhababik360.auth.repository;

import com.marhababik360.auth.config.DatabaseConfig;
import com.marhababik360.auth.exception.ApiException;
import com.marhababik360.auth.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserRepository {
    private final DatabaseConfig databaseConfig;

    public UserRepository(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public User save(User user) {
        String sql = """
                INSERT INTO users(id, full_name, email, password_hash, role, age, cn, phone, category, business_name, profile_photo, created_at, updated_at)
                VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindUser(statement, user);
            statement.executeUpdate();
            return user;
        } catch (SQLException exception) {
            if (exception.getMessage() != null && exception.getMessage().contains("UNIQUE")) {
                throw new ApiException(409, "Conflict", "Email already exists");
            }
            throw new ApiException(500, "Internal Server Error", "Could not save user");
        }
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new ApiException(500, "Internal Server Error", "Could not read user");
        }
    }

    private void bindUser(PreparedStatement statement, User user) throws SQLException {
        statement.setString(1, user.id);
        statement.setString(2, user.fullName);
        statement.setString(3, user.email);
        statement.setString(4, user.passwordHash);
        statement.setString(5, user.role);
        if (user.age == null) statement.setObject(6, null); else statement.setInt(6, user.age);
        statement.setString(7, user.cn);
        statement.setString(8, user.phone);
        statement.setString(9, user.category);
        statement.setString(10, user.businessName);
        statement.setString(11, user.profilePhoto);
        statement.setString(12, user.createdAt);
        statement.setString(13, user.updatedAt);
    }

    private User map(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.id = resultSet.getString("id");
        user.fullName = resultSet.getString("full_name");
        user.email = resultSet.getString("email");
        user.passwordHash = resultSet.getString("password_hash");
        user.role = resultSet.getString("role");
        int age = resultSet.getInt("age");
        user.age = resultSet.wasNull() ? null : age;
        user.cn = resultSet.getString("cn");
        user.phone = resultSet.getString("phone");
        user.category = resultSet.getString("category");
        user.businessName = resultSet.getString("business_name");
        user.profilePhoto = resultSet.getString("profile_photo");
        user.createdAt = resultSet.getString("created_at");
        user.updatedAt = resultSet.getString("updated_at");
        return user;
    }
}
