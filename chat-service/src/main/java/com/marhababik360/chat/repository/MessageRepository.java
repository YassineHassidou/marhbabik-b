package com.marhababik360.chat.repository;

import com.marhababik360.chat.config.DatabaseConfig;
import com.marhababik360.chat.exception.ApiException;
import com.marhababik360.chat.model.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageRepository {
    private final DatabaseConfig databaseConfig;
    public MessageRepository(DatabaseConfig databaseConfig) { this.databaseConfig = databaseConfig; }

    public Message save(Message message) {
        String sql = "INSERT INTO messages(id, sender_id, receiver_id, text, timestamp) VALUES(?, ?, ?, ?, ?)";
        try (Connection connection = databaseConfig.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, message.id);
            statement.setString(2, message.senderId);
            statement.setString(3, message.receiverId);
            statement.setString(4, message.text);
            statement.setString(5, message.timestamp);
            statement.executeUpdate();
            return message;
        } catch (SQLException exception) {
            throw new ApiException(500, "Internal Server Error", "Could not save message");
        }
    }

    public List<Message> conversation(String userId, String receiverId) {
        String sql = """
                SELECT * FROM messages
                WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)
                ORDER BY timestamp ASC
                """;
        try (Connection connection = databaseConfig.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            statement.setString(2, receiverId);
            statement.setString(3, receiverId);
            statement.setString(4, userId);
            try (ResultSet rs = statement.executeQuery()) {
                List<Message> messages = new ArrayList<>();
                while (rs.next()) messages.add(map(rs));
                return messages;
            }
        } catch (SQLException exception) {
            throw new ApiException(500, "Internal Server Error", "Could not read messages");
        }
    }

    private Message map(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.id = rs.getString("id");
        message.senderId = rs.getString("sender_id");
        message.receiverId = rs.getString("receiver_id");
        message.text = rs.getString("text");
        message.timestamp = rs.getString("timestamp");
        return message;
    }
}
