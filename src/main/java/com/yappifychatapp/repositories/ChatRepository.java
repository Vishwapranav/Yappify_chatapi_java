package com.yappifychatapp.repositories;

import com.yappifychatapp.models.Chat;
import com.yappifychatapp.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends MongoRepository<Chat, String> {

    // Find all chats for a user (both one-to-one and group chats)
    List<Chat> findByUsersContainingOrderByUpdatedAtDesc(User user);

    // Find chats where isGroupChat is false and contains a specific user
    List<Chat> findByIsGroupChatAndUsersContaining(Boolean isGroupChat, User user);

    // Find all chats containing a specific user (needed for account deletion)
    List<Chat> findByUsersContaining(User user);
}