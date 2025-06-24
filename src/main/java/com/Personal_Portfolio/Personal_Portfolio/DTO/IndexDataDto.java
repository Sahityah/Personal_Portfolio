package com.Personal_Portfolio.Personal_Portfolio.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndexDataDto {
    private String symbol;
    private Double price;
    private Double change; // Percentage change
}
