package com.yappifychatapp.controllers;

import com.yappifychatapp.models.Chat;
import com.yappifychatapp.services.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat Management", description = "Manage one-to-one and group chats")
@SecurityRequirement(name = "Bearer Authentication")
@SecurityRequirement(name = "User ID Header")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/")
    @Operation(summary = "Create or access one-to-one chat", description = "Create a new chat or get existing chat between two users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chat created or retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Chat> accessChat(
            @Parameter(description = "Logged-in user ID") @RequestHeader("userId") String loggedInUserId,
            @RequestBody Map<String, String> body) {
        String otherUserId = body.get("userId");

        if (otherUserId == null || otherUserId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }

        Chat chat = chatService.accessChat(loggedInUserId, otherUserId);
        return ResponseEntity.ok(chat);
    }

    @GetMapping("/")
    @Operation(summary = "Get all chats", description = "Get all chats for the logged-in user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chats retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<Chat>> fetchChats(
            @Parameter(description = "Logged-in user ID") @RequestHeader("userId") String loggedInUserId) {
        List<Chat> chats = chatService.fetchChats(loggedInUserId);
        return ResponseEntity.ok(chats);
    }

    @GetMapping("/{chatId}")
    @Operation(summary = "Get chat by ID", description = "Get a specific chat by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chat retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Chat not found"),
            @ApiResponse(responseCode = "403", description = "User is not a member of this chat")
    })
    public ResponseEntity<Chat> getChatById(
            @Parameter(description = "Chat ID") @PathVariable String chatId,
            @Parameter(description = "Logged-in user ID") @RequestHeader("userId") String userId) {
        Chat chat = chatService.getChatById(chatId, userId);
        return ResponseEntity.ok(chat);
    }

    @PostMapping("/group")
    @Operation(summary = "Create group chat", description = "Create a new group chat with multiple users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request - need at least 2 users and a name")
    })
    public ResponseEntity<Chat> createGroup(
            @Parameter(description = "Admin user ID") @RequestHeader("userId") String adminId,
            @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        List<String> users = (List<String>) body.get("users");

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Group name is required");
        }

        if (users == null || users.size() < 2) {
            throw new IllegalArgumentException("At least 2 users are required to create a group");
        }

        Chat chat = chatService.createGroupChat(name, users, adminId);
        return ResponseEntity.ok(chat);
    }

    @PutMapping("/rename")
    @Operation(summary = "Rename group", description = "Update the name of a group chat (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group renamed successfully"),
            @ApiResponse(responseCode = "403", description = "Only admin can rename the group"),
            @ApiResponse(responseCode = "404", description = "Chat not found")
    })
    public ResponseEntity<Chat> renameGroup(
            @Parameter(description = "Admin user ID") @RequestHeader("userId") String adminId,
            @RequestBody Map<String, String> body) {
        String chatId = body.get("chatId");
        String chatName = body.get("chatName");

        if (chatName == null || chatName.trim().isEmpty()) {
            throw new IllegalArgumentException("Chat name is required");
        }

        Chat updatedChat = chatService.renameGroup(chatId, chatName, adminId);
        return ResponseEntity.ok(updatedChat);
    }

    @PutMapping("/groupadd")
    @Operation(summary = "Add user to group", description = "Add a user to an existing group chat (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User added successfully"),
            @ApiResponse(responseCode = "403", description = "Only admin can add users"),
            @ApiResponse(responseCode = "404", description = "Chat or user not found"),
            @ApiResponse(responseCode = "400", description = "User is already in the group")
    })
    public ResponseEntity<Chat> addUserToGroup(
            @Parameter(description = "Admin user ID") @RequestHeader("userId") String adminId,
            @RequestBody Map<String, String> body) {
        String chatId = body.get("chatId");
        String userId = body.get("userId");

        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }

        Chat updatedChat = chatService.addUserToGroup(chatId, userId, adminId);
        return ResponseEntity.ok(updatedChat);
    }

    @PutMapping("/groupremove")
    @Operation(summary = "Remove user from group", description = "Remove a user from a group chat (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User removed successfully"),
            @ApiResponse(responseCode = "403", description = "Only admin can remove users"),
            @ApiResponse(responseCode = "404", description = "Chat or user not found"),
            @ApiResponse(responseCode = "400", description = "Cannot remove the last member or admin")
    })
    public ResponseEntity<Chat> removeUserFromGroup(
            @Parameter(description = "Admin user ID") @RequestHeader("userId") String adminId,
            @RequestBody Map<String, String> body) {
        String chatId = body.get("chatId");
        String userId = body.get("userId");

        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }

        Chat updatedChat = chatService.removeUserFromGroup(chatId, userId, adminId);
        return ResponseEntity.ok(updatedChat);
    }

    @PostMapping("/group/{chatId}/leave")
    @Operation(summary = "Leave group", description = "Leave a group chat. If admin leaves, a new admin is assigned.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Left group successfully"),
            @ApiResponse(responseCode = "404", description = "Chat not found"),
            @ApiResponse(responseCode = "400", description = "Cannot leave one-to-one chat or already not a member")
    })
    public ResponseEntity<Map<String, String>> leaveGroup(
            @Parameter(description = "Chat ID") @PathVariable String chatId,
            @Parameter(description = "User ID") @RequestHeader("userId") String userId) {
        chatService.leaveGroup(chatId, userId);
        return ResponseEntity.ok(Map.of("message", "Left group successfully"));
    }

    @DeleteMapping("/group/{chatId}")
    @Operation(summary = "Delete group", description = "Delete/disband a group chat (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Only admin can delete the group"),
            @ApiResponse(responseCode = "404", description = "Chat not found"),
            @ApiResponse(responseCode = "400", description = "Cannot delete one-to-one chat")
    })
    public ResponseEntity<Map<String, String>> deleteGroup(
            @Parameter(description = "Chat ID") @PathVariable String chatId,
            @Parameter(description = "Admin user ID") @RequestHeader("userId") String adminId) {
        chatService.deleteGroup(chatId, adminId);
        return ResponseEntity.ok(Map.of("message", "Group deleted successfully"));
    }

    @PutMapping("/group/{chatId}/transfer-admin")
    @Operation(summary = "Transfer admin rights", description = "Transfer admin rights to another member (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin rights transferred successfully"),
            @ApiResponse(responseCode = "403", description = "Only current admin can transfer rights"),
            @ApiResponse(responseCode = "404", description = "Chat or user not found"),
            @ApiResponse(responseCode = "400", description = "New admin must be a member of the group")
    })
    public ResponseEntity<Chat> transferAdmin(
            @Parameter(description = "Chat ID") @PathVariable String chatId,
            @Parameter(description = "Current admin user ID") @RequestHeader("userId") String currentAdminId,
            @RequestBody Map<String, String> body) {
        String newAdminId = body.get("newAdminId");

        if (newAdminId == null || newAdminId.trim().isEmpty()) {
            throw new IllegalArgumentException("New admin ID is required");
        }

        Chat updatedChat = chatService.transferAdmin(chatId, currentAdminId, newAdminId);
        return ResponseEntity.ok(updatedChat);
    }
}