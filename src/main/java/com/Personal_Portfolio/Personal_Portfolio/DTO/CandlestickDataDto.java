package com.Personal_Portfolio.Personal_Portfolio.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandlestickDataDto {
    private String time; // Date string for X-axis (e.g., "YYYY-MM-DD")
    private Double open;
    private Double high;
    private Double low;
    private Double close;
}
