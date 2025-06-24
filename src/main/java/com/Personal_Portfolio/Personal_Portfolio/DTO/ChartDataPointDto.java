package com.Personal_Portfolio.Personal_Portfolio.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataPointDto {
    private LocalDateTime timestamp;
    private double value;
}