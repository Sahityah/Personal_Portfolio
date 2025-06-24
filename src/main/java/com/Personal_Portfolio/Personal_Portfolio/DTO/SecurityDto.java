package com.Personal_Portfolio.Personal_Portfolio.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityDto {
    private String symbol;
    private String name;
    private String type; // e.g., "equity", "fno", "index"
}
