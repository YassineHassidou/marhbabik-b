package com.marhababik360.user.repository;

import com.marhababik360.user.config.DatabaseConfig;
import com.marhababik360.user.exception.ApiException;
import com.marhababik360.user.model.User;

import java.sql.*;
import java.util.Optional;

public class UserRepository {
    private final DatabaseConfig databaseConfig;
    public UserRepository(DatabaseConfig databaseConfig) { this.databaseConfig = databaseConfig; }

    public Optional<User> findById(String id) {
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE id = ?")) {
            statement.setString(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new ApiException(500, "Internal Server Error", "Could not read user");
        }
    }

    public User save(User user) {
        String sql = """
                INSERT INTO users(id, full_name, email, role, age, cn, phone, category, business_name, profile_photo, created_at, updated_at)
                VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, user);
            statement.executeUpdate();
            return user;
        } catch (SQLException exception) {
            throw new ApiException(500, "Internal Server Error", "Could not save user");
        }
    }

    public User update(User user) {
        String sql = """
                UPDATE users SET full_name = ?, email = ?, role = ?, age = ?, cn = ?, phone = ?, category = ?,
                business_name = ?, profile_photo = ?, updated_at = ? WHERE id = ?
                """;
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.fullName);
            statement.setString(2, user.email);
            statement.setString(3, user.role);
            if (user.age == null) statement.setObject(4, null); else statement.setInt(4, user.age);
            statement.setString(5, user.cn);
            statement.setString(6, user.phone);
            statement.setString(7, user.category);
            statement.setString(8, user.businessName);
            statement.setString(9, user.profilePhoto);
            statement.setString(10, user.updatedAt);
            statement.setString(11, user.id);
            statement.executeUpdate();
            return user;
        } catch (SQLException exception) {
            throw new ApiException(500, "Internal Server Error", "Could not update user");
        }
    }

    private void bind(PreparedStatement statement, User user) throws SQLException {
        statement.setString(1, user.id);
        statement.setString(2, user.fullName);
        statement.setString(3, user.email);
        statement.setString(4, user.role);
        if (user.age == null) statement.setObject(5, null); else statement.setInt(5, user.age);
        statement.setString(6, user.cn);
        statement.setString(7, user.phone);
        statement.setString(8, user.category);
        statement.setString(9, user.businessName);
        statement.setString(10, user.profilePhoto);
        statement.setString(11, user.createdAt);
        statement.setString(12, user.updatedAt);
    }

    private User map(ResultSet rs) throws SQLException {
        User user = new User();
        user.id = rs.getString("id");
        user.fullName = rs.getString("full_name");
        user.email = rs.getString("email");
        user.role = rs.getString("role");
        int age = rs.getInt("age");
        user.age = rs.wasNull() ? null : age;
        user.cn = rs.getString("cn");
        user.phone = rs.getString("phone");
        user.category = rs.getString("category");
        user.businessName = rs.getString("business_name");
        user.profilePhoto = rs.getString("profile_photo");
        user.createdAt = rs.getString("created_at");
        user.updatedAt = rs.getString("updated_at");
        return user;
    }
}
