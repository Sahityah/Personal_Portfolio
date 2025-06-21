package com.Personal_Portfolio.Personal_Portfolio.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;
    private String strike;
    private String type;     // CALL, PUT, PE, CE etc.
    private Integer quantity;
    private Double entryPrice;
    private Double ltp;
    private Double pnl;
    private Double mtm;

    @ManyToOne
    private User user;
}

