package com.yappifychatapp.dto;

import lombok.Data;

@Data
public class ChatMessageDTO {
    private String chatId;
    private String senderId;
    private String content;
}
