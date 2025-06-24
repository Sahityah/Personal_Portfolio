package com.Personal_Portfolio.Personal_Portfolio.Service;

import com.Personal_Portfolio.Personal_Portfolio.Entity.*;
import com.Personal_Portfolio.Personal_Portfolio.DTO.*;
import com.Personal_Portfolio.Personal_Portfolio.Security.GoogleTokenVerifier;
import com.Personal_Portfolio.Personal_Portfolio.Repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
// import com.auth0.jwt.JWT; // Not directly used in this snippet
// import com.auth0.jwt.interfaces.DecodedJWT; // Not directly used in this snippet

import java.time.LocalDateTime; // Import for LocalDateTime
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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService; // Assuming JwtService is defined elsewhere

    public User register(RegisterRequest request) {
        // Check if email is already registered
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            // Throw a custom exception for better error handling
            throw new RuntimeException("Email already exists."); // Consider using UserAlreadyExistsException
        }

        // Create new user entity and set provider as EMAIL
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .provider(User.Provider.EMAIL)
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

    public User googleLogin(GoogleLoginRequest request) {
        try {
            // Assuming GoogleTokenVerifier.verifyToken is correctly implemented
            GoogleIdToken.Payload payload = GoogleTokenVerifier.verifyToken(request.getCredential());
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            // String picture = (String) payload.get("picture"); // Not used in User entity currently

            // Check if user exists
            Optional<User> optionalUser = userRepository.findByEmail(email);
            User user;

            if (optionalUser.isPresent()) {
                user = optionalUser.get();
                // If existing user logs in via Google, ensure their provider is set to GOOGLE if it wasn't already
                if (user.getProvider() != User.Provider.GOOGLE) {
                    user.setProvider(User.Provider.GOOGLE);
                }
            } else {
                // Register new user for Google login
                user = new User();
                user.setUsername(name);
                user.setEmail(email);
                user.setProvider(User.Provider.GOOGLE);
                // For Google authenticated users, password field can remain null or empty as they authenticate via Google
            }

            // Update lastLogin timestamp for both new and existing Google users
            user.setLastLogin(LocalDateTime.now());
            // Save user to persist changes (new user or updated lastLogin/provider for existing)
            return userRepository.save(user);

        } catch (Exception e) {
            throw new RuntimeException("Google login failed: " + e.getMessage(), e); // Include original exception for debugging
        }
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
        return dto;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null; // Or throw an exception if user must be authenticated
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found in DB."));
    }

}
