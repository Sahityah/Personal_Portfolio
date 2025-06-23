package com.Personal_Portfolio.Personal_Portfolio.Service;

import com.Personal_Portfolio.Personal_Portfolio.Entity.Portfolio;
import com.Personal_Portfolio.Personal_Portfolio.Entity.Position;
import com.Personal_Portfolio.Personal_Portfolio.Entity.Trade;
import com.Personal_Portfolio.Personal_Portfolio.Entity.User;
import com.Personal_Portfolio.Personal_Portfolio.Repository.PortfolioRepository;
import com.Personal_Portfolio.Personal_Portfolio.Repository.PositionRepository;
import com.Personal_Portfolio.Personal_Portfolio.Repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepository tradeRepository;
    private final PositionRepository positionRepository;
    private final PortfolioRepository portfolioRepository; // To interact with Portfolio data
    private final KafkaTemplate<String, Trade> kafkaTemplate; // Kafka for asynchronous trade processing

    private static final String USER_TRADE_COMMANDS_TOPIC = "user-trades-commands";
    private static final String ORDER_STATUS_UPDATES_TOPIC = "order-status-updates";

    // Method to initiate a trade request from the API
    @Transactional
    public Trade requestTrade(Long userId, String symbol, String tradeType, String instrumentType, int quantity, double price,
                              String strikePrice, String expiryDate) {
        User user = new User(); // Simplified: In a real app, fetch user from DB or security context
        user.setId(userId);

        Trade trade = new Trade();
        trade.setUser(user);
        trade.setSymbol(symbol);
        trade.setTradeType(tradeType);
        trade.setInstrumentType(instrumentType);
        trade.setQuantity(quantity);
        trade.setPrice(price);
        trade.setStrikePrice(strikePrice);
        trade.setExpiryDate(expiryDate);
        trade.setTradeTime(LocalDateTime.now());
        trade.setStatus("PENDING"); // Initial status

        tradeRepository.save(trade); // Persist the request

        // Publish trade command to Kafka for asynchronous execution
        kafkaTemplate.send(USER_TRADE_COMMANDS_TOPIC, userId.toString(), trade);

        return trade;
    }

    // This method is called by the Kafka Consumer (TradeCommandConsumer)
    @Transactional
    public void processTradeExecution(Trade trade) {
        // --- Core Trade Execution Logic ---
        // This is where you would integrate with a brokerage API or simulate execution.
        // For a real system, this logic can be complex (margin checks, real-time prices, order routing).

        Portfolio portfolio = portfolioRepository.findByUserId(trade.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Portfolio not found for user: " + trade.getUser().getId()));

        double tradeCost = trade.getQuantity() * trade.getPrice();

        // Basic margin/fund check (highly simplified)
        if (trade.getTradeType().equalsIgnoreCase("BUY") && portfolio.getMarginAvailable() < tradeCost) {
            trade.setStatus("REJECTED");
            trade.setRemarks("Insufficient margin.");
            tradeRepository.save(trade);
            kafkaTemplate.send(ORDER_STATUS_UPDATES_TOPIC, trade.getUser().getId().toString(), trade);
            return;
        }

        Optional<Position> existingPosition = positionRepository.findByUserIdAndSymbolAndInstrumentTypeAndStrikePriceAndExpiryDate(
                trade.getUser().getId(), trade.getSymbol(), trade.getInstrumentType(), trade.getStrikePrice(), trade.getExpiryDate());

        Position positionToUpdate;

        if (trade.getTradeType().equalsIgnoreCase("BUY")) {
            if (existingPosition.isPresent()) {
                positionToUpdate = existingPosition.get();
                // Calculate new weighted average entry price
                double currentTotalCost = positionToUpdate.getAverageEntryPrice() * positionToUpdate.getQuantity();
                double newTotalCost = currentTotalCost + tradeCost;
                int newQuantity = positionToUpdate.getQuantity() + trade.getQuantity();
                positionToUpdate.setAverageEntryPrice(newTotalCost / newQuantity);
                positionToUpdate.setQuantity(newQuantity);
            } else {
                positionToUpdate = new Position();
                positionToUpdate.setUser(trade.getUser());
                positionToUpdate.setSymbol(trade.getSymbol());
                positionToUpdate.setInstrumentType(trade.getInstrumentType());
                positionToUpdate.setStrikePrice(trade.getStrikePrice());
                positionToUpdate.setExpiryDate(trade.getExpiryDate());
                positionToUpdate.setQuantity(trade.getQuantity());
                positionToUpdate.setAverageEntryPrice(trade.getPrice());
                positionToUpdate.setCurrentLtp(trade.getPrice()); // Set initial LTP to trade price
                positionToUpdate.setUnrealizedPnl(0.0);
                positionToUpdate.setMtm(0.0);
            }
            // Deduct funds for BUY
            portfolio.setMarginAvailable(portfolio.getMarginAvailable() - tradeCost);

        } else if (trade.getTradeType().equalsIgnoreCase("SELL")) {
            if (existingPosition.isPresent()) {
                positionToUpdate = existingPosition.get();
                if (positionToUpdate.getQuantity() >= trade.getQuantity()) {
                    positionToUpdate.setQuantity(positionToUpdate.getQuantity() - trade.getQuantity());
                    // Add funds for SELL
                    portfolio.setMarginAvailable(portfolio.getMarginAvailable() + tradeCost);
                    // If quantity becomes 0, calculate realized P&L and delete position
                    if (positionToUpdate.getQuantity() == 0) {
                        // Realized PnL: (Sale Price - Avg Entry Price) * Quantity Sold
                        double realizedPnl = (trade.getPrice() - positionToUpdate.getAverageEntryPrice()) * trade.getQuantity();
                        // For simplicity, add to a realized PnL tracker if you had one.
                        // portfolio.setRealizedPnl(portfolio.getRealizedPnl() + realizedPnl);
                        positionRepository.delete(positionToUpdate);
                        positionToUpdate = null; // Mark for no save
                    }
                } else {
                    trade.setStatus("REJECTED");
                    trade.setRemarks("Insufficient quantity to sell.");
                    tradeRepository.save(trade);
                    kafkaTemplate.send(ORDER_STATUS_UPDATES_TOPIC, trade.getUser().getId().toString(), trade);
                    return;
                }
            } else {
                trade.setStatus("REJECTED");
                trade.setRemarks("No open position found to sell for symbol: " + trade.getSymbol());
                tradeRepository.save(trade);
                kafkaTemplate.send(ORDER_STATUS_UPDATES_TOPIC, trade.getUser().getId().toString(), trade);
                return;
            }
        } else {
            trade.setStatus("REJECTED");
            trade.setRemarks("Invalid trade type: " + trade.getTradeType());
            tradeRepository.save(trade);
            kafkaTemplate.send(ORDER_STATUS_UPDATES_TOPIC, trade.getUser().getId().toString(), trade);
            return;
        }

        // Save/Update Position and Portfolio
        if (positionToUpdate != null) {
            positionRepository.save(positionToUpdate);
        }
        portfolioRepository.save(portfolio); // Save updated margin

        trade.setStatus("FILLED");
        tradeRepository.save(trade); // Update trade status

        // Send a Kafka message about the filled order for other consumers/frontend updates
        kafkaTemplate.send(ORDER_STATUS_UPDATES_TOPIC, trade.getUser().getId().toString(), trade);
    }

    public List<Trade> getTradesByUserId(Long userId) {
        return tradeRepository.findByUserIdOrderByTradeTimeDesc(userId);
    }

    public Trade getTradeById(Long tradeId) {
        return tradeRepository.findById(tradeId)
                .orElseThrow(() -> new RuntimeException("Trade not found with ID: " + tradeId));
    }
}
