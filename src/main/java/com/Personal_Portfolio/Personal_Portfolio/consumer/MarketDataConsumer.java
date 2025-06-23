package com.Personal_Portfolio.Personal_Portfolio.consumer;

import com.Personal_Portfolio.Personal_Portfolio.Entity.Position;
import com.Personal_Portfolio.Personal_Portfolio.Repository.PositionRepository;
import com.Personal_Portfolio.Personal_Portfolio.Service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MarketDataConsumer {

    private final PositionRepository positionRepository;
    private final PortfolioService portfolioService;

    @KafkaListener(topics = "market-data-feed", groupId = "stock-dashboard-group", containerFactory = "kafkaListenerContainerFactory")
    public void listenMarketData(Map<String, Object> marketData) {
        String symbol = (String) marketData.get("symbol");
        Double ltp = ((Number) marketData.get("ltp")).doubleValue(); // Handle potential Integer/Double from JSON
        // Long timestamp = ((Number) marketData.get("timestamp")).longValue(); // If you need timestamp

        if (symbol != null && ltp != null) {
            System.out.println("Received live market data for " + symbol + ": LTP = " + ltp);

            // Find all open positions for this symbol across all users
            List<Position> positionsToUpdate = positionRepository.findBySymbol(symbol);

            for (Position position : positionsToUpdate) {
                // Update each relevant position's LTP and recalculate P&L/MTM
                portfolioService.updatePositionLTPAndCalculatePnL(position.getId(), ltp);
            }
        }
    }
}