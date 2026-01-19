package com.yappifychatapp.controllers;

import com.yappifychatapp.dto.ChatMessageDTO;
import com.yappifychatapp.models.Message;
import com.yappifychatapp.services.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final MessageService messageService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDTO chatMessage,
                            SimpMessageHeaderAccessor headerAccessor) {
        log.info("Received WebSocket message - ChatId: {}, SenderId: {}",
                chatMessage.getChatId(), chatMessage.getSenderId());

        try {
            // Save message to DB and publish to Kafka
            // Kafka consumer will handle broadcasting to WebSocket subscribers
            Message message = messageService.sendMessage(
                    chatMessage.getSenderId(),
                    chatMessage.getChatId(),
                    chatMessage.getContent()
            );

            log.info("Message processed successfully - MessageId: {}", message.getId());
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage());
            // You can send error back to the client if needed
        }
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessageDTO chatMessage,
                        SimpMessageHeaderAccessor headerAccessor) {
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("userId", chatMessage.getSenderId());
        log.info("User connected - UserId: {}", chatMessage.getSenderId());
    }
}