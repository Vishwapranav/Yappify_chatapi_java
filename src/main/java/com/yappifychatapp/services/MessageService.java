package com.yappifychatapp.services;

import com.yappifychatapp.dto.KafkaMessageDTO;
import com.yappifychatapp.models.Chat;
import com.yappifychatapp.models.Message;
import com.yappifychatapp.models.User;
import com.yappifychatapp.repositories.ChatRepository;
import com.yappifychatapp.repositories.MessageRepository;
import com.yappifychatapp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final KafkaProducerService kafkaProducerService;

    private static final long EDIT_TIME_LIMIT_MINUTES = 15;

    // Fetch all messages for a chat with pagination and membership validation
    public Page<Message> getAllMessages(String chatId, String userId, Pageable pageable) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        // Validate user is a member of the chat
        boolean isMember = chat.getUsers().stream()
                .anyMatch(user -> user.getId().equals(userId));

        if (!isMember) {
            throw new RuntimeException("You are not a member of this chat");
        }

        return messageRepository.findByChatIdOrderByCreatedAtDesc(chatId, pageable);
    }

    // Send a message with Kafka integration
    @Transactional
    public Message sendMessage(String senderId, String chatId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Message content cannot be empty");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        // Validate sender is a member of the chat
        boolean isMember = chat.getUsers().stream()
                .anyMatch(user -> user.getId().equals(senderId));

        if (!isMember) {
            throw new RuntimeException("You are not a member of this chat");
        }

        // Create and save message to database
        Message message = new Message();
        message.setSender(sender);
        message.setChat(chat);
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());

        Message savedMessage = messageRepository.save(message);

        // Update latestMessage in chat
        chat.setLatestMessage(savedMessage);
        chatRepository.save(chat);

        // Publish message to Kafka for async processing
        publishToKafka(savedMessage);

        log.info("Message saved and published to Kafka - MessageId: {}", savedMessage.getId());

        return savedMessage;
    }

    // Edit message (sender only, within 15 minutes)
    @Transactional
    public Message editMessage(String messageId, String userId, String newContent) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // Validate sender
        if (!message.getSender().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only edit your own messages");
        }

        // Check time limit (15 minutes)
        long minutesSinceSent = ChronoUnit.MINUTES.between(message.getCreatedAt(), LocalDateTime.now());
        if (minutesSinceSent > EDIT_TIME_LIMIT_MINUTES) {
            throw new IllegalArgumentException("Cannot edit message after " + EDIT_TIME_LIMIT_MINUTES + " minutes");
        }

        message.setContent(newContent);
        message.setEdited(true);
        message.setEditedAt(LocalDateTime.now());

        Message updatedMessage = messageRepository.save(message);
        log.info("Message edited - MessageId: {}", messageId);

        return updatedMessage;
    }

    // Delete message (sender or group admin)
    @Transactional
    public void deleteMessage(String messageId, String userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        Chat chat = message.getChat();

        boolean isSender = message.getSender().getId().equals(userId);
        boolean isAdmin = chat.getIsGroupChat() &&
                chat.getGroupAdmin() != null &&
                chat.getGroupAdmin().getId().equals(userId);

        if (!isSender && !isAdmin) {
            throw new IllegalArgumentException("You can only delete your own messages" +
                    (chat.getIsGroupChat() ? " or as group admin" : ""));
        }

        messageRepository.delete(message);

        // If this was the latest message, update chat's latestMessage
        if (chat.getLatestMessage() != null && chat.getLatestMessage().getId().equals(messageId)) {
            // Find the new latest message
            List<Message> recentMessages = messageRepository.findTop1ByChatOrderByCreatedAtDesc(chat);
            chat.setLatestMessage(recentMessages.isEmpty() ? null : recentMessages.get(0));
            chatRepository.save(chat);
        }

        log.info("Message deleted - MessageId: {} by User: {}", messageId, userId);
    }

    // Mark message as read
    @Transactional
    public void markAsRead(String messageId, String userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // Validate user is a member of the chat
        Chat chat = message.getChat();
        boolean isMember = chat.getUsers().stream()
                .anyMatch(user -> user.getId().equals(userId));

        if (!isMember) {
            throw new RuntimeException("You are not a member of this chat");
        }

        // Add user to readBy list if not already there
        if (!message.getReadBy().stream().anyMatch(user -> user.getId().equals(userId))) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            message.getReadBy().add(user);
            messageRepository.save(message);
            log.info("Message marked as read - MessageId: {} by User: {}", messageId, userId);
        }
    }

    // Get unread message count
    public long getUnreadCount(String chatId, String userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        // Validate user is a member
        boolean isMember = chat.getUsers().stream()
                .anyMatch(user -> user.getId().equals(userId));

        if (!isMember) {
            throw new RuntimeException("You are not a member of this chat");
        }

        return messageRepository.countUnreadMessages(chatId, userId);
    }

    private void publishToKafka(Message message) {
        KafkaMessageDTO kafkaMessage = new KafkaMessageDTO(
                message.getId(),
                message.getChat().getId(),
                message.getSender().getId(),
                message.getSender().getName(),
                message.getContent(),
                message.getCreatedAt() != null ? message.getCreatedAt() : LocalDateTime.now(),
                message.getChat().getIsGroupChat()
        );

        kafkaProducerService.sendMessage(kafkaMessage);
    }
}