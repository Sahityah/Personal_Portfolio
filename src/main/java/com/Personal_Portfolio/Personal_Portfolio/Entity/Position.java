package com.Personal_Portfolio.Personal_Portfolio.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a trading position held by a user.
 * This entity maps to a table in the database and stores details about each position.
 */
@Entity // Marks this class as a JPA entity
@Table(name = "positions") // Specifies the table name in the database
@Data // Lombok annotation to generate getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Lombok annotation to generate a no-argument constructor
@AllArgsConstructor // Lombok annotation to generate an all-argument constructor
public class Position {

    @Id // Marks the 'id' field as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configures ID generation strategy (auto-increment)
    private Long id; // Unique identifier for the position

    @ManyToOne(fetch = FetchType.LAZY) // Many positions can belong to one user
    @JoinColumn(name = "user_id", nullable = false) // Specifies the foreign key column in the 'positions' table
    private User user; // The user who owns this position

    @Column(nullable = false) // Ensures the 'symbol' field cannot be null
    private String symbol; // Trading symbol (e.g., "RELIANCE", "NIFTY")

    private String strike; // Strike price for F&O options, can be null for equity

    @Column(nullable = false)
    private String type; // Instrument type (e.g., "CALL", "PUT", "FUT", "EQ")

    @Column(nullable = false)
    private String segment; // Market segment (e.g., "equity", "fno")

    @Column(nullable = false)
    private Integer qty; // Quantity of the instrument held

    @Column(nullable = false)
    private Double entryPrice; // Price at which the position was entered

    private Double ltp; // Last Traded Price (can be updated dynamically)

    private Double pnl; // Profit and Loss for the position

    private Double mtm; // Mark-to-Market value of the position

    @Column(nullable = false)
    private String status; // Status of the position (e.g., "active", "closed")

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
    // Additional fields can be added as per business requirements, e.g., tradeDate, exitPrice, etc.
}
