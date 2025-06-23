package com.Personal_Portfolio.Personal_Portfolio.Repository;

import com.Personal_Portfolio.Personal_Portfolio.Entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position, Long> {
    List<Position> findByUserId(Long userId);
    Optional<Position> findByUserIdAndSymbolAndInstrumentTypeAndStrikePriceAndExpiryDate(
            Long userId, String symbol, String instrumentType, String strikePrice, String expiryDate);

    // For updating market data across all users holding a specific symbol
    List<Position> findBySymbol(String symbol);
}
