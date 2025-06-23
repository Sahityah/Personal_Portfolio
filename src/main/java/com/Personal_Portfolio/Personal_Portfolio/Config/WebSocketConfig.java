package com.Personal_Portfolio.Personal_Portfolio.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory broker
        // /topic for general broadcast messages (e.g., global market updates)
        // /queue for point-to-point messages (e.g., specific user portfolio updates)
        config.enableSimpleBroker("/topic", "/queue", "/user"); // Added /user for user-specific destinations
        config.setApplicationDestinationPrefixes("/app"); // Prefix for messages from client to server
        config.setUserDestinationPrefix("/user"); // Designates user-specific destinations
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // The endpoint clients will use to connect to WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOrigins("${cors.allowed-origins}".split(",")) // Allow CORS from configured origins
                .withSockJS(); // Use SockJS for broader browser support
    }
}