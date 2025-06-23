package com.Personal_Portfolio.Personal_Portfolio.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockSymbol {
    private String symbol;
    private String companyName;
    private String isin;
    private String sector;
    // You can add more fields if your CSV has them
}