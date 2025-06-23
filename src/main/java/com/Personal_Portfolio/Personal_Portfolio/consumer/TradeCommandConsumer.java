package com.Personal_Portfolio.Personal_Portfolio.consumer;
import com.Personal_Portfolio.Personal_Portfolio.Entity.Trade;
import com.Personal_Portfolio.Personal_Portfolio.Service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TradeCommandConsumer {

    private final TradeService tradeService;

    @KafkaListener(topics = "user-trades-commands", groupId = "stock-dashboard-group", containerFactory = "kafkaListenerContainerFactory")
    public void listenUserTradeCommands(Trade trade) {
        System.out.println("Received trade command for user " + trade.getUser().getId() + ": " + trade);
        try {
            tradeService.processTradeExecution(trade);
            System.out.println("Trade processed successfully: " + trade.getId());
        } catch (Exception e) {
            System.err.println("Error processing trade command " + trade.getId() + ": " + e.getMessage());
            // In a real system, send to a dead-letter topic or retry
        }
    }
}
