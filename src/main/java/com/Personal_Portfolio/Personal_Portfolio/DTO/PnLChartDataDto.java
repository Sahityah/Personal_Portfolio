package com.Personal_Portfolio.Personal_Portfolio.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PnLChartDataDto {
    private List<ChartDataPointDto> daily;
    private List<ChartDataPointDto> weekly;
    private List<ChartDataPointDto> monthly;
    private List<ChartDataPointDto> yearly;
}