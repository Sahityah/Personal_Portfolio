package com.Personal_Portfolio.Personal_Portfolio.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a user's portfolio.
 * This entity primarily links a user to their collection of positions
 * and can optionally store high-level, aggregated portfolio information.
 */
@Entity // Marks this class as a JPA entity
@Table(name = "portfolios") // Specifies the table name in the database
@Data // Lombok annotation to generate getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Lombok annotation to generate a no-argument constructor
@AllArgsConstructor // Lombok annotation to generate an all-argument constructor
public class Portfolio {

    @Id // Marks the 'id' field as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configures ID generation strategy (auto-increment)
    private Long id; // Unique identifier for the portfolio

    @OneToOne(fetch = FetchType.LAZY) // One-to-one relationship with the User entity
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true) // Foreign key column
    private User user; // The user who owns this portfolio

    @Column(nullable = false)
    private String name; // A name for the portfolio, e.g., "Main Portfolio"

    // While `PortfolioDataDto` calculates dynamic values like total investment and PnL,
    // the `Portfolio` entity itself could optionally store some aggregated, less frequently changing
    // financial metrics or metadata about the portfolio, if needed for persistence or reporting.
    // For example:
    // private String currency;
    // private String description;

    @CreationTimestamp // Automatically sets the creation timestamp
    @Column(updatable = false) // Ensures this field is not updated after creation
    private LocalDateTime createdAt;

    @UpdateTimestamp // Automatically updates the timestamp on entity modification
    private LocalDateTime updatedAt;
}