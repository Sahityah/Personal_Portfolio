package com.Personal_Portfolio.Personal_Portfolio.DTO;

import lombok.Data;

@Data
public class TradeRequest {
    private String symbol;
    private String tradeType; // BUY, SELL
    private String instrumentType; // EQ, CALL, PUT, FUT
    private int quantity;
    private double price;
    private String strikePrice;   // Nullable for EQ
    private String expiryDate;    // Nullable for EQ
}
