package com.yappifychatapp.services;

import com.yappifychatapp.models.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    // userId -> WebSocketSession
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void registerSession(String userId, WebSocketSession session) {
        sessions.put(userId, session);
    }

    public void removeSession(String userId) {
        sessions.remove(userId);
    }

    // Broadcast message to all chat users except sender
    public void broadcastMessage(Message message) {
        List<String> userIds = message.getChat().getUsers().stream()
                .map(u -> u.getId())
                .toList();

        userIds.forEach(uid -> {
            if (!uid.equals(message.getSender().getId()) && sessions.containsKey(uid)) {
                try {
                    sessions.get(uid).sendMessage(new TextMessage(message.getContent()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
