package com.Personal_Portfolio.Personal_Portfolio.consumer;

import com.Personal_Portfolio.Personal_Portfolio.Entity.Trade;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderStatusUpdateConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "order-status-updates", groupId = "stock-dashboard-group", containerFactory = "kafkaListenerContainerFactory")
    public void listenOrderStatusUpdates(Trade trade) {
        System.out.println("Received order status update for trade " + trade.getId() + ": " + trade.getStatus());
        // Send real-time trade status update to the specific user's frontend via WebSocket
        messagingTemplate.convertAndSendToUser(
                trade.getUser().getId().toString(),
                "/topic/trade-status",
                trade
        );
    }
}