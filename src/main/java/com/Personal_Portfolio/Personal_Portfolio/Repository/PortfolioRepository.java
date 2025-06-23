package com.Personal_Portfolio.Personal_Portfolio.Repository;

import com.Personal_Portfolio.Personal_Portfolio.Entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    Optional<Portfolio> findByUserId(Long userId);
}
