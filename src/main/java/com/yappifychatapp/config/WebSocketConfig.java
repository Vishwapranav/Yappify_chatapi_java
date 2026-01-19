package com.yappifychatapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // fallback for browsers without WebSocket
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable a simple in-memory broker for /topic and /queue
        registry.enableSimpleBroker("/topic", "/queue");

        // Messages sent to /app will be routed to @MessageMapping methods
        registry.setApplicationDestinationPrefixes("/app");

        // For user-specific messages
        registry.setUserDestinationPrefix("/user");
    }
}