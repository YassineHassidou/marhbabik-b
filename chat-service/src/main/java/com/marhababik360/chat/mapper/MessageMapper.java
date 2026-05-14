package com.marhababik360.chat.mapper;

import com.marhababik360.chat.dto.MessageResponse;
import com.marhababik360.chat.model.Message;

public final class MessageMapper {
    private MessageMapper() {}
    public static MessageResponse toResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.id = message.id;
        response.senderId = message.senderId;
        response.receiverId = message.receiverId;
        response.text = message.text;
        response.timestamp = message.timestamp;
        return response;
    }
}
