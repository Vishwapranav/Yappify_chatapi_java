package com.yappifychatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KafkaMessageDTO {
    private String messageId;
    private String chatId;
    private String senderId;
    private String senderName;
    private String content;
    private LocalDateTime timestamp;
    private boolean isGroupChat;
}