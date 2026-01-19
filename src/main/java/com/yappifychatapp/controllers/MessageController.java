package com.yappifychatapp.controllers;

import com.yappifychatapp.models.Message;
import com.yappifychatapp.services.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
@Tag(name = "Message Management", description = "Send, retrieve, edit and delete chat messages")
@SecurityRequirement(name = "Bearer Authentication")
@SecurityRequirement(name = "User ID Header")
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/{chatId}")
    @Operation(summary = "Get all messages with pagination",
            description = "Get all messages for a specific chat with pagination support")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Messages retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Chat not found"),
            @ApiResponse(responseCode = "403", description = "User is not a member of this chat")
    })
    public ResponseEntity<Page<Message>> getAllMessages(
            @Parameter(description = "Chat ID") @PathVariable String chatId,
            @Parameter(description = "User ID") @RequestHeader("userId") String userId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<Message> messages = messageService.getAllMessages(chatId, userId, pageable);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/")
    @Operation(summary = "Send message", description = "Send a new message to a chat")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message sent successfully"),
            @ApiResponse(responseCode = "400", description = "Empty message content"),
            @ApiResponse(responseCode = "404", description = "Chat or sender not found"),
            @ApiResponse(responseCode = "403", description = "User is not a member of this chat")
    })
    public ResponseEntity<Message> sendMessage(
            @Parameter(description = "Sender user ID") @RequestHeader("userId") String senderId,
            @RequestBody Map<String, String> body) {
        String chatId = body.get("chatId");
        String content = body.get("content");

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }

        Message message = messageService.sendMessage(senderId, chatId, content);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/{messageId}")
    @Operation(summary = "Edit message", description = "Edit an existing message (only by sender, within 15 minutes)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message edited successfully"),
            @ApiResponse(responseCode = "403", description = "Cannot edit message - not the sender or time limit exceeded"),
            @ApiResponse(responseCode = "404", description = "Message not found"),
            @ApiResponse(responseCode = "400", description = "Empty message content")
    })
    public ResponseEntity<Message> editMessage(
            @Parameter(description = "Message ID") @PathVariable String messageId,
            @Parameter(description = "User ID") @RequestHeader("userId") String userId,
            @RequestBody Map<String, String> body) {
        String newContent = body.get("content");

        if (newContent == null || newContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }

        Message updatedMessage = messageService.editMessage(messageId, userId, newContent);
        return ResponseEntity.ok(updatedMessage);
    }

    @DeleteMapping("/{messageId}")
    @Operation(summary = "Delete message", description = "Delete a message (only by sender or group admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Cannot delete message - not authorized"),
            @ApiResponse(responseCode = "404", description = "Message not found")
    })
    public ResponseEntity<Map<String, String>> deleteMessage(
            @Parameter(description = "Message ID") @PathVariable String messageId,
            @Parameter(description = "User ID") @RequestHeader("userId") String userId) {
        messageService.deleteMessage(messageId, userId);
        return ResponseEntity.ok(Map.of("message", "Message deleted successfully"));
    }

    @PostMapping("/{messageId}/read")
    @Operation(summary = "Mark message as read", description = "Mark a message as read by the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message marked as read"),
            @ApiResponse(responseCode = "404", description = "Message not found"),
            @ApiResponse(responseCode = "403", description = "User is not a member of this chat")
    })
    public ResponseEntity<Map<String, String>> markAsRead(
            @Parameter(description = "Message ID") @PathVariable String messageId,
            @Parameter(description = "User ID") @RequestHeader("userId") String userId) {
        messageService.markAsRead(messageId, userId);
        return ResponseEntity.ok(Map.of("message", "Marked as read"));
    }

    @GetMapping("/unread/{chatId}")
    @Operation(summary = "Get unread message count", description = "Get count of unread messages for a chat")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unread count retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Chat not found")
    })
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @Parameter(description = "Chat ID") @PathVariable String chatId,
            @Parameter(description = "User ID") @RequestHeader("userId") String userId) {
        long unreadCount = messageService.getUnreadCount(chatId, userId);
        return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
    }
}