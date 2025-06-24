package com.Personal_Portfolio.Personal_Portfolio.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
// import org.hibernate.annotations.UpdateTimestamp; // Not used for lastLogin if updated manually

import java.time.LocalDateTime;
import java.util.Set;

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

    @CreationTimestamp // This will automatically set the creation timestamp
    @Column(nullable = false, updatable = false) // Make it non-nullable and non-updatable after creation
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime lastLogin; // This will be updated manually in the UserService on login

    @Size(max = 255, message = "Avatar URL must be less than 255 characters")
    private String avatar;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian phone number")
    private String phone;

    @Size(max = 255, message = "Address must be less than 255 characters")
    private String address;

    @Size(max = 50, message = "City must be less than 50 characters")
    private String city;

    @Size(max = 50, message = "State must be less than 50 characters")
    private String state;

    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid Indian ZIP code")
    private String zip;

    // The 'role' field has been removed as per your request.
    // If you need roles in the future, consider using an Enum for better type safety.

    @Enumerated(EnumType.STRING)
    @Column(name = "provider")
    private Provider provider;

    public enum Provider {
        EMAIL,
        GOOGLE
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Position> positions;


}
