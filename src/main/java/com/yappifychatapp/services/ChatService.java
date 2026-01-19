package com.yappifychatapp.services;

import com.yappifychatapp.models.Chat;
import com.yappifychatapp.models.User;
import com.yappifychatapp.repositories.ChatRepository;
import com.yappifychatapp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    // Create or fetch one-to-one chat
    public Chat accessChat(String loggedInUserId, String otherUserId) {
        User loggedInUser = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get all one-to-one chats for the logged-in user
        List<Chat> userChats = chatRepository.findByIsGroupChatAndUsersContaining(false, loggedInUser);

        // Find if a chat already exists with the other user
        for (Chat chat : userChats) {
            if (chat.getUsers().contains(otherUser)) {
                return chat;
            }
        }

        // Create new one-to-one chat if not found
        Chat chat = new Chat();
        chat.setChatName("sender");
        chat.setIsGroupChat(false);
        List<User> users = new ArrayList<>();
        users.add(loggedInUser);
        users.add(otherUser);
        chat.setUsers(users);

        return chatRepository.save(chat);
    }

    // Fetch all chats for a user
    public List<Chat> fetchChats(String loggedInUserId) {
        User loggedInUser = userRepository.findById(loggedInUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return chatRepository.findByUsersContainingOrderByUpdatedAtDesc(loggedInUser);
    }

    // Get chat by ID with membership validation
    public Chat getChatById(String chatId, String userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        // Validate user is a member of the chat
        boolean isMember = chat.getUsers().stream()
                .anyMatch(user -> user.getId().equals(userId));

        if (!isMember) {
            throw new RuntimeException("You are not a member of this chat");
        }

        return chat;
    }

    // Create group chat
    public Chat createGroupChat(String groupName, List<String> userIds, String adminId) {
        if (userIds.size() < 2) {
            throw new RuntimeException("More than 2 users are required to form a group chat");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        List<User> users = new ArrayList<>();
        for (String id : userIds) {
            User u = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found: " + id));
            users.add(u);
        }

        users.add(admin); // Add admin to group

        Chat groupChat = new Chat();
        groupChat.setChatName(groupName);
        groupChat.setIsGroupChat(true);
        groupChat.setUsers(users);
        groupChat.setGroupAdmin(admin);

        return chatRepository.save(groupChat);
    }

    // Rename group chat (admin only)
    public Chat renameGroup(String chatId, String newName, String adminId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        if (!chat.getIsGroupChat()) {
            throw new IllegalArgumentException("This is not a group chat");
        }

        // Validate admin
        if (chat.getGroupAdmin() == null || !chat.getGroupAdmin().getId().equals(adminId)) {
            throw new IllegalArgumentException("Only admin can rename the group");
        }

        chat.setChatName(newName);
        return chatRepository.save(chat);
    }

    // Add user to group (admin only)
    public Chat addUserToGroup(String chatId, String userId, String adminId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        if (!chat.getIsGroupChat()) {
            throw new IllegalArgumentException("This is not a group chat");
        }

        // Validate admin
        if (chat.getGroupAdmin() == null || !chat.getGroupAdmin().getId().equals(adminId)) {
            throw new IllegalArgumentException("Only admin can add users to the group");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user is already in group
        boolean alreadyMember = chat.getUsers().stream()
                .anyMatch(u -> u.getId().equals(userId));

        if (alreadyMember) {
            throw new IllegalArgumentException("User is already in the group");
        }

        chat.getUsers().add(user);
        return chatRepository.save(chat);
    }

    // Remove user from group (admin only)
    public Chat removeUserFromGroup(String chatId, String userId, String adminId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        if (!chat.getIsGroupChat()) {
            throw new IllegalArgumentException("This is not a group chat");
        }

        // Validate admin
        if (chat.getGroupAdmin() == null || !chat.getGroupAdmin().getId().equals(adminId)) {
            throw new IllegalArgumentException("Only admin can remove users from the group");
        }

        // Cannot remove admin
        if (userId.equals(adminId)) {
            throw new IllegalArgumentException("Admin cannot be removed. Transfer admin rights first.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        chat.getUsers().remove(user);

        // Prevent removing the last member
        if (chat.getUsers().isEmpty()) {
            throw new IllegalArgumentException("Cannot remove the last member. Delete the group instead.");
        }

        return chatRepository.save(chat);
    }

    // Leave group
    public void leaveGroup(String chatId, String userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        if (!chat.getIsGroupChat()) {
            throw new IllegalArgumentException("Cannot leave one-to-one chat");
        }

        // Check if user is a member
        boolean isMember = chat.getUsers().stream()
                .anyMatch(user -> user.getId().equals(userId));

        if (!isMember) {
            throw new IllegalArgumentException("You are not a member of this group");
        }

        // Remove user from members
        chat.getUsers().removeIf(user -> user.getId().equals(userId));

        // If admin leaves, assign new admin from remaining members
        if (chat.getGroupAdmin() != null && chat.getGroupAdmin().getId().equals(userId)) {
            if (!chat.getUsers().isEmpty()) {
                chat.setGroupAdmin(chat.getUsers().get(0));
                log.info("Admin left group. New admin assigned: {}", chat.getUsers().get(0).getName());
            }
        }

        // If no members left, delete the chat
        if (chat.getUsers().isEmpty()) {
            chatRepository.delete(chat);
            log.info("Last member left. Group deleted: {}", chatId);
        } else {
            chatRepository.save(chat);
            log.info("User {} left group {}", userId, chatId);
        }
    }

    // Delete group (admin only)
    public void deleteGroup(String chatId, String adminId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        if (!chat.getIsGroupChat()) {
            throw new IllegalArgumentException("Cannot delete one-to-one chat");
        }

        // Validate admin
        if (chat.getGroupAdmin() == null || !chat.getGroupAdmin().getId().equals(adminId)) {
            throw new IllegalArgumentException("Only admin can delete the group");
        }

        chatRepository.delete(chat);
        log.info("Group deleted by admin: {}", chatId);
    }

    // Transfer admin rights
    public Chat transferAdmin(String chatId, String currentAdminId, String newAdminId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        if (!chat.getIsGroupChat()) {
            throw new IllegalArgumentException("This is not a group chat");
        }

        // Validate current admin
        if (chat.getGroupAdmin() == null || !chat.getGroupAdmin().getId().equals(currentAdminId)) {
            throw new IllegalArgumentException("Only current admin can transfer admin rights");
        }

        // Validate new admin is a member
        boolean isMember = chat.getUsers().stream()
                .anyMatch(user -> user.getId().equals(newAdminId));

        if (!isMember) {
            throw new IllegalArgumentException("New admin must be a member of the group");
        }

        User newAdmin = userRepository.findById(newAdminId)
                .orElseThrow(() -> new RuntimeException("New admin not found"));

        chat.setGroupAdmin(newAdmin);
        log.info("Admin rights transferred from {} to {} in group {}", currentAdminId, newAdminId, chatId);

        return chatRepository.save(chat);
    }
}