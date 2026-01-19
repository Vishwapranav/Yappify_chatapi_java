package com.yappifychatapp.services;

import com.yappifychatapp.dto.KafkaMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.chat-messages}")
    private String chatMessagesTopic;

    public void sendMessage(KafkaMessageDTO messageDTO) {
        log.info("Sending message to Kafka topic: {}", chatMessagesTopic);

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(chatMessagesTopic, messageDTO.getChatId(), messageDTO);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Message sent successfully to topic: {} with offset: {}",
                        chatMessagesTopic, result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send message to Kafka: {}", ex.getMessage());
            }
        });
    }

    public void sendMessageSync(KafkaMessageDTO messageDTO) {
        try {
            SendResult<String, Object> result =
                    kafkaTemplate.send(chatMessagesTopic, messageDTO.getChatId(), messageDTO).get();
            log.info("Message sent synchronously to topic: {} with offset: {}",
                    chatMessagesTopic, result.getRecordMetadata().offset());
        } catch (Exception e) {
            log.error("Failed to send message synchronously: {}", e.getMessage());
            throw new RuntimeException("Failed to send message to Kafka", e);
        }
    }
}