package com.Personal_Portfolio.Personal_Portfolio.Repository;

import com.Personal_Portfolio.Personal_Portfolio.Entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByUserIdOrderByTradeTimeDesc(Long userId);
}
