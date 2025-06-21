package com.Personal_Portfolio.Personal_Portfolio.Service;

import com.Personal_Portfolio.Personal_Portfolio.Entity.*;
import com.Personal_Portfolio.Personal_Portfolio.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepository tradeRepository;
    private final PositionRepository positionRepository;
    private final UserRepository userRepository;

    public Trade addTrade(Trade trade) {
        // Set timestamp
        trade.setTimestamp(LocalDateTime.now());

        // Save Trade
        Trade savedTrade = tradeRepository.save(trade);

        // Fetch User
        User user = userRepository.findById(trade.getUser().getId()).orElseThrow();

        // Check for existing Position
        List<Position> positions = positionRepository.findByUserId(user.getId());
        Position position = positions.stream()
                .filter(p -> p.getSymbol().equals(trade.getSymbol()))
                .findFirst()
                .orElse(null);

        if (position == null) {
            // New Position
            position = Position.builder()
                    .symbol(trade.getSymbol())
                    .strike("-")
                    .type("-")
                    .quantity(trade.getQuantity())
                    .entryPrice(trade.getPrice())
                    .ltp(trade.getPrice())
                    .pnl(0.0)
                    .mtm(0.0)
                    .user(user)
                    .build();
        } else {
            // Update existing Position
            int newQty = position.getQuantity() + (trade.getType().equalsIgnoreCase("BUY") ? trade.getQuantity() : -trade.getQuantity());
            position.setQuantity(newQty);

            // Update average Entry Price (simple way)
            double totalValue = (position.getEntryPrice() * position.getQuantity()) + (trade.getPrice() * trade.getQuantity());
            int totalQty = position.getQuantity() + trade.getQuantity();
            position.setEntryPrice(totalValue / totalQty);
        }

        // Update P&L and MTM (assuming LTP = Trade Price for now)
        position.setLtp(trade.getPrice());
        double pnl = (position.getLtp() - position.getEntryPrice()) * position.getQuantity();
        position.setPnl(pnl);
        position.setMtm(pnl);

        positionRepository.save(position);

        // Update User's Networth and Margin (simple logic)
        user.setNetworth(user.getNetworth() - (trade.getType().equalsIgnoreCase("BUY") ? trade.getPrice() * trade.getQuantity() : 0));
        user.setMarginAvailable(user.getMarginAvailable() - (trade.getType().equalsIgnoreCase("BUY") ? trade.getPrice() * trade.getQuantity() * 0.2 : 0)); // example margin 20%
        userRepository.save(user);

        return savedTrade;
    }

    public List<Trade> getTradesByUser(Long userId) {
        return tradeRepository.findByUserId(userId);
    }
}
