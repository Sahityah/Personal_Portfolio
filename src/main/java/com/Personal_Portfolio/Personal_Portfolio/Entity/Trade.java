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
public class Trade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String symbol;
    @Column(nullable = false)
    private String tradeType; // BUY, SELL
    private String instrumentType; // EQ, CALL, PUT, FUT
    private String strikePrice; // For options/futures
    private String expiryDate; // For options/futures

    private int quantity;
    private double price; // Executed price of the trade
    private LocalDateTime tradeTime;

    private String status; // PENDING, FILLED, REJECTED, CANCELED
    private String remarks; // Reason for rejection/cancellation

    @PrePersist
    protected void onCreate() {
        tradeTime = LocalDateTime.now();
    }
}
