package com.Personal_Portfolio.Personal_Portfolio.Service;

import com.Personal_Portfolio.Personal_Portfolio.Entity.User;
import com.Personal_Portfolio.Personal_Portfolio.DTO.*;
// import com.Personal_Portfolio.Personal_Portfolio.Security.GoogleTokenVerifier; // No longer needed for this flow
import com.Personal_Portfolio.Personal_Portfolio.Repository.UserRepository;
// import com.fasterxml.jackson.databind.JsonNode; // No longer needed
// import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken; // No longer needed
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
// import org.springframework.beans.factory.annotation.Value; // Not directly needed for Google client-specific properties here
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestTemplate; // No longer needed
import org.springframework.security.oauth2.core.user.OAuth2User; // Import for Spring Security OAuth2 User

import java.time.LocalDateTime;
import java.util.Optional;

// Placeholder for custom exceptions. You should define these classes in a separate package (e.g., com.Personal_Portfolio.Personal_Portfolio.Exception)
// public class UserAlreadyExistsException extends RuntimeException {
//     public UserAlreadyExistsException(String message) { super(message); }
// }
// public class InvalidCredentialsException extends RuntimeException {
//     public InvalidCredentialsException(String message) { super(message); }
// }

@Service
@RequiredArgsConstructor
public class UserService {

    // These Google OAuth properties are now primarily used by Spring Security internally
    // and don't need to be injected into UserService for the standard flow.
    // They are defined in application.properties.
    // @Value("${google.oauth.client-id}")
    // private String googleClientId;
    // @Value("${google.oauth.client-secret}")
    // private String googleClientSecret;
    // @Value("${google.oauth.redirect-uri}")
    // private String googleRedirectUri;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService; // Assuming JwtService is defined elsewhere

    // RestTemplate is no longer needed in UserService for Google OAuth as Spring Security handles the API calls.
    // private final RestTemplate restTemplate = new RestTemplate();


    public User register(RegisterRequest request) {
        // Check if email is already registered
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists."); // Consider using UserAlreadyExistsException
        }

        // Create new user entity and set provider as EMAIL
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .provider(User.Provider.EMAIL) // Set provider type
                .build();

        // Save user and return
        return userRepository.save(user);
    }


    public User login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password.")); // Consider using InvalidCredentialsException

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password."); // Consider using InvalidCredentialsException
        }

        // Update lastLogin timestamp
        user.setLastLogin(LocalDateTime.now());
        // Save the user to persist the lastLogin update
        return userRepository.save(user);
    }

    /**
     * This method is for handling users authenticated via Spring Security's OAuth2 flow (e.g., Google).
     * It finds an existing user by email or creates a new one if not found.
     * This replaces the manual Google token verification and API calls.
     *
     * @param oauth2User The authenticated OAuth2User object provided by Spring Security.
     * @return The User entity from your application's database.
     */
    public User findOrCreateOAuth2User(OAuth2User oauth2User) {
        // Extract email and name from the OAuth2User attributes
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        // Basic validation (email is crucial for identification)
        if (email == null) {
            throw new IllegalArgumentException("OAuth2 user email not found. Cannot process Google login.");
        }

        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            // Update user details if necessary (e.g., name might have changed on Google)
            if (name != null && !name.equals(user.getUsername())) {
                user.setUsername(name);
            }
            // Ensure provider is set to GOOGLE if they are now logging in via Google
            if (user.getProvider() != User.Provider.GOOGLE) {
                user.setProvider(User.Provider.GOOGLE);
            }
        } else {
            // Create a new user for Google login
            user = new User();
            user.setEmail(email);
            user.setUsername(name != null ? name : email); // Use email as username if name is null
            user.setProvider(User.Provider.GOOGLE); // Set provider type
            // For social logins, you typically don't store a password, or store a random placeholder
            // that will never be used for direct password login. This avoids password management issues.
            // Ensure your User entity's password field can be nullable or has a suitable default for social users.
            // If it cannot be null, store a very long random hash.
            user.setPassword(passwordEncoder.encode("GOOGLE_SOCIAL_PLACEHOLDER_" + System.currentTimeMillis() + Math.random())); // Secure random placeholder
            // Set any default roles or other initial properties
        }

        // Update lastLogin timestamp for both new and existing Google users
        user.setLastLogin(LocalDateTime.now());
        // Save user to persist changes (new user or updated lastLogin/provider/name for existing)
        return userRepository.save(user);
    }


    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public UserDto getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
        return convertToDto(user);
    }

    @Transactional
    public UserDto updateUserProfile(String email, UserDto userDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        user.setUsername(userDto.getUsername());
        user.setPhone(userDto.getPhone());
        user.setAddress(userDto.getAddress());
        user.setCity(userDto.getCity());
        user.setState(userDto.getState());
        user.setZip(userDto.getZip());
        // Do not allow email update directly here, or handle carefully if allowed
        // Do not update password here

        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    @Transactional
    public void changePassword(String email, PasswordChangeRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        // Only allow password change if the user's provider is EMAIL (local login)
        // Social users should change their password via their social provider (Google, etc.)
        if (user.getProvider() != User.Provider.EMAIL) {
            throw new BadCredentialsException("Password change not allowed for social login users. Please change via your social provider.");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password does not match.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // Helper to convert User entity to UserDto
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setAvatar(user.getAvatar());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setCity(user.getCity());
        dto.setState(user.getState());
        dto.setZip(user.getZip());
        // Potentially add provider information to DTO if frontend needs to know
        return dto;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null; // Or throw an exception if user must be authenticated
        }
        String email = authentication.getName(); // For OAuth2 users, this will be the email from Google
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found in DB."));
    }
}