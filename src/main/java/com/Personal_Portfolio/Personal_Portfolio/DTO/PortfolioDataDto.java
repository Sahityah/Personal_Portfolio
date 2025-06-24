package com.Personal_Portfolio.Personal_Portfolio.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioDataDto {
    private Double networth;
    private Double todayMTM;
    private Double marginAvailable;
    private Double equityValue;
    private Double fnoValue;
    private List<PositionDto> positions;
    private Double marginUsed;
    private Double todayEquityPnL;
    private Double todayFnoPnL;

    public PortfolioDataDto(double totalInvestment, double currentValue, double totalPnL, double dailyPnL, double utilizedMargin, double totalMargin) {
    }
}