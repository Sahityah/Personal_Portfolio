package com.Personal_Portfolio.Personal_Portfolio.Controller;

import com.Personal_Portfolio.Personal_Portfolio.DTO.LoginRequest;
import com.Personal_Portfolio.Personal_Portfolio.DTO.RegisterRequest;
import com.Personal_Portfolio.Personal_Portfolio.Entity.User;
import com.Personal_Portfolio.Personal_Portfolio.Service.JwtService;
import com.Personal_Portfolio.Personal_Portfolio.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value; // This can be removed if frontendBaseUrl is not used in this simplified controller
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
// Removed org.springframework.web.reactive.result.view.RedirectView as it's not used here anymore

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    // These dependencies are typically used within UserService or SecurityConfig,
    // not directly in these specific controller methods.
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    // frontendBaseUrl is no longer directly used in this controller after removing the Google callback
    // @Value("${app.frontend.url}")
    // private String frontendBaseUrl;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        logger.info("Attempting to register user: {}", request.getEmail());
        try {
            User user = userService.register(request);
            String token = jwtService.generateToken(user);
            logger.info("User registered successfully: {}", user.getEmail());
            // Consider returning a more structured AuthResponse DTO here for consistency.
            return ResponseEntity.ok().body(token);
        } catch (Exception e) {
            logger.error("Registration failed for user {}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        logger.info("Attempting to log in user: {}", request.getEmail());
        try {
            // userService.login should handle authentication using authenticationManager
            User user = userService.login(request);
            String token = jwtService.generateToken(user);
            logger.info("User logged in successfully: {}", user.getEmail());
            // Consider returning a more structured AuthResponse DTO here for consistency.
            return ResponseEntity.ok().body(token);
        } catch (Exception e) {
            logger.error("Login failed for user {}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.badRequest().body("Login failed: " + e.getMessage());
        }
    }

    // This endpoint is removed.
    // If you explicitly intended to receive an ID Token from the frontend (a different flow),
    // you would implement ID Token verification here.
    // For the standard Spring Security OAuth2 authorization code flow, this is not needed.
    // @PostMapping("/google-login")
    // public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequest request) {
    //    // ...
    // }

    // This endpoint is removed.
    // Spring Security's OAuth2 Client module automatically handles the GET request
    // to /login/oauth2/code/google and performs the authorization code exchange.
    // Your custom logic for processing the Google user should be in SecurityConfig's success handler.
    // @GetMapping("/login/oauth2/code/google")
    // public RedirectView googleOAuth2Callback(...) {
    //    // ...
    // }
}