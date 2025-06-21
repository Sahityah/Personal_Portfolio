package com.Personal_Portfolio.Personal_Portfolio.DTO;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportDto {
    private Double totalPnl;
    private Double totalMtm;
    private Double networth;
    private Double marginAvailable;
}

