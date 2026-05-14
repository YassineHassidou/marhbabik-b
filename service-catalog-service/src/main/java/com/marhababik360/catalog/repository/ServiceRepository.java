package com.marhababik360.catalog.repository;

import com.google.gson.reflect.TypeToken;
import com.marhababik360.catalog.config.DatabaseConfig;
import com.marhababik360.catalog.config.JsonConfig;
import com.marhababik360.catalog.exception.ApiException;
import com.marhababik360.catalog.model.Service;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServiceRepository {
    private static final Type STRING_LIST = new TypeToken<List<String>>() {}.getType();
    private final DatabaseConfig databaseConfig;
    public ServiceRepository(DatabaseConfig databaseConfig) { this.databaseConfig = databaseConfig; }

    public List<Service> findAll(String category, String workerId) {
        StringBuilder sql = new StringBuilder("SELECT * FROM services WHERE 1 = 1");
        List<String> args = new ArrayList<>();
        if (category != null && !category.isBlank()) { sql.append(" AND category = ?"); args.add(category); }
        if (workerId != null && !workerId.isBlank()) { sql.append(" AND worker_id = ?"); args.add(workerId); }
        sql.append(" ORDER BY created_at DESC");
        try (Connection connection = databaseConfig.getConnection(); PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < args.size(); i++) statement.setString(i + 1, args.get(i));
            try (ResultSet rs = statement.executeQuery()) {
                List<Service> services = new ArrayList<>();
                while (rs.next()) services.add(map(rs));
                return services;
            }
        } catch (SQLException exception) {
            throw new ApiException(500, "Internal Server Error", "Could not read services");
        }
    }

    public Optional<Service> findById(String id) {
        try (Connection connection = databaseConfig.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM services WHERE id = ?")) {
            statement.setString(1, id);
            try (ResultSet rs = statement.executeQuery()) { return rs.next() ? Optional.of(map(rs)) : Optional.empty(); }
        } catch (SQLException exception) {
            throw new ApiException(500, "Internal Server Error", "Could not read service");
        }
    }

    public Service save(Service service) {
        String sql = "INSERT INTO services(id,title,category,description,price,images,location,worker_id,worker_name,created_at,updated_at) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection connection = databaseConfig.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, service);
            statement.executeUpdate();
            return service;
        } catch (SQLException exception) {
            throw new ApiException(500, "Internal Server Error", "Could not save service");
        }
    }

    public Service update(Service service) {
        String sql = "UPDATE services SET title=?, category=?, description=?, price=?, images=?, location=?, worker_id=?, worker_name=?, created_at=?, updated_at=? WHERE id=?";
        try (Connection connection = databaseConfig.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, service.title);
            statement.setString(2, service.category);
            statement.setString(3, service.description);
            statement.setDouble(4, service.price);
            statement.setString(5, JsonConfig.gson().toJson(service.images));
            statement.setString(6, service.location);
            statement.setString(7, service.workerId);
            statement.setString(8, service.workerName);
            statement.setString(9, service.createdAt);
            statement.setString(10, service.updatedAt);
            statement.setString(11, service.id);
            statement.executeUpdate();
            return service;
        } catch (SQLException exception) {
            throw new ApiException(500, "Internal Server Error", "Could not update service");
        }
    }

    public void delete(String id) {
        try (Connection connection = databaseConfig.getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM services WHERE id = ?")) {
            statement.setString(1, id);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new ApiException(500, "Internal Server Error", "Could not delete service");
        }
    }

    private void bind(PreparedStatement statement, Service service) throws SQLException {
        statement.setString(1, service.id);
        statement.setString(2, service.title);
        statement.setString(3, service.category);
        statement.setString(4, service.description);
        statement.setDouble(5, service.price);
        statement.setString(6, JsonConfig.gson().toJson(service.images));
        statement.setString(7, service.location);
        statement.setString(8, service.workerId);
        statement.setString(9, service.workerName);
        statement.setString(10, service.createdAt);
        statement.setString(11, service.updatedAt);
    }

    private Service map(ResultSet rs) throws SQLException {
        Service service = new Service();
        service.id = rs.getString("id");
        service.title = rs.getString("title");
        service.category = rs.getString("category");
        service.description = rs.getString("description");
        service.price = rs.getDouble("price");
        service.images = JsonConfig.gson().fromJson(rs.getString("images"), STRING_LIST);
        if (service.images == null) service.images = new ArrayList<>();
        service.location = rs.getString("location");
        service.workerId = rs.getString("worker_id");
        service.workerName = rs.getString("worker_name");
        service.createdAt = rs.getString("created_at");
        service.updatedAt = rs.getString("updated_at");
        return service;
    }
}
