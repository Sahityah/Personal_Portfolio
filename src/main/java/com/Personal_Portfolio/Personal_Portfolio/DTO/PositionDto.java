package com.Personal_Portfolio.Personal_Portfolio.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PositionDto {
    private Long id;
    private String symbol;
    private String strike;
    private String type;
    private String segment;
    private Integer qty;
    private Double entryPrice;
    private Double ltp;
    private Double pnl;
    private Double mtm;
    private String status;
}
