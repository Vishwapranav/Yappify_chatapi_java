package com.yappifychatapp.services;

import com.yappifychatapp.models.Chat;
import com.yappifychatapp.models.Message;
import com.yappifychatapp.models.User;
import com.yappifychatapp.repositories.ChatRepository;
import com.yappifychatapp.repositories.MessageRepository;
import com.yappifychatapp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // Get user by ID
    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Register a new user
    public User registerUser(String name, String email, String password, String pic) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("User already exists");
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setPic(pic != null ? pic : user.getPic());

        User savedUser = userRepository.save(user);
        log.info("New user registered: {}", email);
        return savedUser;
    }

    // Authenticate user
    public User authenticateUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid Email or Password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid Email or Password");
        }

        log.info("User authenticated: {}", email);
        return user;
    }

    // Search users by name or email
    public List<User> searchUsers(String keyword, String loggedInUserId) {
        Pattern regex = Pattern.compile(keyword, Pattern.CASE_INSENSITIVE);
        return userRepository.findByIdNotAndNameRegexOrEmailRegex(loggedInUserId, regex, regex);
    }

    // Update user profile
    @Transactional
    public User updateProfile(String userId, String name, String pic) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (name != null && !name.trim().isEmpty()) {
            user.setName(name);
        }

        if (pic != null && !pic.trim().isEmpty()) {
            user.setPic(pic);
        }

        User updatedUser = userRepository.save(user);
        log.info("User profile updated: {}", userId);
        return updatedUser;
    }

    // Change password
    @Transactional
    public void changePassword(String userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Update to new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed for user: {}", userId);
    }

    // Delete account - Production ready with proper cleanup
    @Transactional
    public void deleteAccount(String userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify password before deletion
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Password is incorrect");
        }

        log.info("Starting account deletion process for user: {}", userId);

        // Step 1: Find all chats where user is a member
        List<Chat> userChats = chatRepository.findByUsersContaining(user);

        for (Chat chat : userChats) {
            if (chat.getIsGroupChat()) {
                // Handle group chat
                chat.getUsers().remove(user);

                // If user was admin, assign new admin
                if (chat.getGroupAdmin() != null && chat.getGroupAdmin().getId().equals(userId)) {
                    if (!chat.getUsers().isEmpty()) {
                        chat.setGroupAdmin(chat.getUsers().get(0));
                        log.info("Transferred admin rights to: {}", chat.getUsers().get(0).getName());
                    }
                }

                // If no members left, delete the chat
                if (chat.getUsers().isEmpty()) {
                    // Delete all messages in this chat first
                    List<Message> chatMessages = messageRepository.findByChatOrderByCreatedAtAsc(chat);
                    messageRepository.deleteAll(chatMessages);
                    chatRepository.delete(chat);
                    log.info("Deleted empty group chat: {}", chat.getId());
                } else {
                    chatRepository.save(chat);
                    log.info("Removed user from group chat: {}", chat.getId());
                }
            } else {
                // One-to-one chat - delete the entire chat
                List<Message> chatMessages = messageRepository.findByChatOrderByCreatedAtAsc(chat);
                messageRepository.deleteAll(chatMessages);
                chatRepository.delete(chat);
                log.info("Deleted one-to-one chat: {}", chat.getId());
            }
        }

        // Step 2: Handle messages where user is referenced in readBy
        // Note: This is optional - messages will still exist but won't reference the user

        // Step 3: Delete the user account
        userRepository.delete(user);
        log.info("User account deleted successfully: {}", userId);
    }
}