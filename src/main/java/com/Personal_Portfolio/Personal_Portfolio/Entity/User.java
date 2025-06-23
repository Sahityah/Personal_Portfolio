package com.Personal_Portfolio.Personal_Portfolio.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @JsonIgnore // Do not expose password hash via API
    @Column(unique = true)
    private String email;

    private String password;

    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }


    private String role; // ADMIN, USER

    @Enumerated(EnumType.STRING)
    @Column(name = "provider")
    private Provider provider;

    public enum Provider {
        EMAIL,
        GOOGLE
    }


}