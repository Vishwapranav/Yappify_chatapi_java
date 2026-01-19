package com.yappifychatapp.services;

import com.yappifychatapp.dto.KafkaMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(
            topics = "${kafka.topic.chat-messages}",
            groupId = "chat-consumer-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeMessage(@Payload KafkaMessageDTO messageDTO,
                               @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                               @Header(KafkaHeaders.OFFSET) long offset) {

        if (messageDTO == null) {
            log.warn("Received null message from Kafka - partition: {}, offset: {}", partition, offset);
            return;
        }

        log.info("Received message from Kafka - ChatId: {}, SenderId: {}, Partition: {}, Offset: {}",
                messageDTO.getChatId(), messageDTO.getSenderId(), partition, offset);

        try {
            // Broadcast to all subscribers of this chat
            String destination = "/topic/chat/" + messageDTO.getChatId();
            messagingTemplate.convertAndSend(destination, messageDTO);

            log.info("Message broadcasted to WebSocket destination: {}", destination);
        } catch (Exception e) {
            log.error("Error broadcasting message to WebSocket: {}", e.getMessage(), e);
            // Message is still marked as consumed, won't retry
        }
    }

    @KafkaListener(
            topics = "${kafka.topic.chat-messages}",
            groupId = "chat-notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeForNotifications(@Payload KafkaMessageDTO messageDTO,
                                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                        @Header(KafkaHeaders.OFFSET) long offset) {

        if (messageDTO == null) {
            log.warn("Received null message for notifications - partition: {}, offset: {}", partition, offset);
            return;
        }

        log.info("Processing notification for message in chat: {} - Partition: {}, Offset: {}",
                messageDTO.getChatId(), partition, offset);

        // Here you can add notification logic:
        // - Push notifications
        // - Email notifications
        // - Update unread counts
        // - etc.
    }
}