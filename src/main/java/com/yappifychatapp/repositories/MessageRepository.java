package com.yappifychatapp.repositories;

import com.yappifychatapp.models.Message;
import com.yappifychatapp.models.Chat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {

    // Original method - find by Chat object
    List<Message> findByChatOrderByCreatedAtAsc(Chat chat);

    // New method - find by Chat ID with pagination (descending order for recent messages first)
    @Query("{ 'chat.$id': ?0 }")
    Page<Message> findByChatIdOrderByCreatedAtDesc(String chatId, Pageable pageable);

    // Count unread messages for a user in a chat
    @Query(value = "{ 'chat.$id': ?0, 'readBy': { $not: { $elemMatch: { '$id': ?1 } } } }", count = true)
    long countUnreadMessages(String chatId, String userId);

    // Find latest message for a chat (used when deleting latest message)
    @Query("{ 'chat': ?0 }")
    List<Message> findTop1ByChatOrderByCreatedAtDesc(Chat chat);
}