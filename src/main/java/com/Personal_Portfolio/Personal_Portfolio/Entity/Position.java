package com.Personal_Portfolio.Personal_Portfolio.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String symbol; // Stock symbol (e.g., RELIANCE, TCS)
    private String instrumentType; // EQ (Equity), CALL, PUT, FUT (Future)
    private String strikePrice;   // For options/futures
    private String expiryDate;    // For options/futures (e.g., "YYYY-MM-DD")

    private int quantity;
    private double averageEntryPrice; // Weighted average entry price
    private double currentLtp;       // Last Traded Price (updated by market data)

    private double unrealizedPnl;    // Profit/Loss not yet booked (based on currentLtp)
    private double mtm;              // Mark-to-Market (often same as unrealizedPnl for open positions)

    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}