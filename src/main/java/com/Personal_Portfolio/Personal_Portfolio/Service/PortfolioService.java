package com.Personal_Portfolio.Personal_Portfolio.Service;

import com.Personal_Portfolio.Personal_Portfolio.Entity.Portfolio;
import com.Personal_Portfolio.Personal_Portfolio.Entity.Position;
import com.Personal_Portfolio.Personal_Portfolio.Entity.User;
import com.Personal_Portfolio.Personal_Portfolio.Repository.PortfolioRepository;
import com.Personal_Portfolio.Personal_Portfolio.Repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PositionRepository positionRepository;
    private final SimpMessagingTemplate messagingTemplate; // For sending WebSocket updates

    @Transactional
    public Portfolio createInitialPortfolio(User user) {
        Portfolio portfolio = new Portfolio();
        portfolio.setUser(user);
        portfolio.setCurrentNetworth(0.0); // Initial net worth
        portfolio.setTodayMtm(0.0);
        portfolio.setMarginAvailable(100000.0); // Example initial margin
        return portfolioRepository.save(portfolio);
    }

    public Portfolio getPortfolioByUserId(Long userId) {
        return portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found for user ID: " + userId));
    }

    public List<Position> getOpenPositionsByUserId(Long userId) {
        return positionRepository.findByUserId(userId);
    }

    // This method is called by the MarketDataConsumer
    @Transactional
    public void updatePositionLTPAndCalculatePnL(Long positionId, double currentLtp) {
        Optional<Position> optionalPosition = positionRepository.findById(positionId);
        if (optionalPosition.isPresent()) {
            Position position = optionalPosition.get();
            position.setCurrentLtp(currentLtp);

            // Calculate Unrealized PnL: (Current Price - Average Entry Price) * Quantity
            double unrealizedPnl = (currentLtp - position.getAverageEntryPrice()) * position.getQuantity();
            position.setUnrealizedPnl(unrealizedPnl);
            position.setMtm(unrealizedPnl); // For simplicity, MTM can be same as unrealized PnL for open positions

            positionRepository.save(position);

            // Recalculate portfolio MTM and networth
            recalculateAndPublishPortfolio(position.getUser().getId());

            // Send real-time update to frontend via WebSocket
            messagingTemplate.convertAndSendToUser(
                    position.getUser().getId().toString(),
                    "/topic/positions",
                    position
            );
        }
    }

    // Recalculates overall portfolio values and pushes updates
    @Transactional
    public void recalculateAndPublishPortfolio(Long userId) {
        Portfolio portfolio = getPortfolioByUserId(userId);
        List<Position> positions = positionRepository.findByUserId(userId);

        double totalCurrentValue = 0.0;
        double totalTodayMtm = 0.0;

        for (Position pos : positions) {
            totalCurrentValue += pos.getCurrentLtp() * pos.getQuantity();
            totalTodayMtm += pos.getMtm(); // Sum of individual position MTMs
        }

        // Add initial cash/margin to total networth calculation
        // For simplicity, assuming margin available is part of net worth if not used
        portfolio.setCurrentNetworth(totalCurrentValue + portfolio.getMarginAvailable()); // Adjust as per your business logic
        portfolio.setTodayMtm(totalTodayMtm);

        portfolioRepository.save(portfolio);

        // Send real-time portfolio update to frontend via WebSocket
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/topic/portfolio",
                portfolio
        );
    }
}