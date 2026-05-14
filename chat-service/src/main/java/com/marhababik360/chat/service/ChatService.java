package com.marhababik360.chat.service;

import com.marhababik360.chat.dto.MessageResponse;
import com.marhababik360.chat.dto.SendMessageRequest;
import com.marhababik360.chat.dto.UserContext;
import com.marhababik360.chat.exception.ApiException;
import com.marhababik360.chat.mapper.MessageMapper;
import com.marhababik360.chat.model.Message;
import com.marhababik360.chat.repository.MessageRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ChatService {
    private final MessageRepository messageRepository;
    public ChatService(MessageRepository messageRepository) { this.messageRepository = messageRepository; }

    public MessageResponse send(UserContext context, SendMessageRequest request) {
        if (request == null) throw new ApiException(400, "Bad Request", "Request body is required");
        if (isBlank(request.receiverId)) throw new ApiException(400, "Bad Request", "receiverId is required");
        if (isBlank(request.text)) throw new ApiException(400, "Bad Request", "text is required");
        if (request.receiverId.equals(context.userId)) throw new ApiException(400, "Bad Request", "Cannot send a message to yourself");
        Message message = new Message();
        message.id = UUID.randomUUID().toString();
        message.senderId = context.userId;
        message.receiverId = request.receiverId.trim();
        message.text = request.text.trim();
        message.timestamp = Instant.now().toString();
        return MessageMapper.toResponse(messageRepository.save(message));
    }

    public List<MessageResponse> conversation(UserContext context, String receiverId) {
        if (isBlank(receiverId)) throw new ApiException(400, "Bad Request", "receiverId query parameter is required");
        return messageRepository.conversation(context.userId, receiverId.trim()).stream().map(MessageMapper::toResponse).toList();
    }

    private boolean isBlank(String value) { return value == null || value.trim().isEmpty(); }
}
