package com.Personal_Portfolio.Personal_Portfolio.Controller;

import com.Personal_Portfolio.Personal_Portfolio.DTO.RegisterRequest;
import com.Personal_Portfolio.Personal_Portfolio.Entity.User;
import com.Personal_Portfolio.Personal_Portfolio.Service.JwtService;
import com.Personal_Portfolio.Personal_Portfolio.Service.UserService;
import com.Personal_Portfolio.Personal_Portfolio.Config.LoginRequest;
import com.Personal_Portfolio.Personal_Portfolio.Exception.UserNotFoundException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        User savedUser = userService.registerUser(user);
        logger.info("New user registered: {}", savedUser.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body) {
        String credential = body.get("credential");

        GoogleIdToken.Payload payload = jwtService.verifyGoogleToken(credential);
        if (payload == null) {
            logger.warn("Invalid Google token attempt");
            return ResponseEntity.badRequest().body("Invalid Google token");
        }

        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        User user = userService.findByEmail(email).orElse(null);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setRole("USER");
            user.setProvider("google");
            // set a dummy hashed password for consistency
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

            userService.saveUser(user);
            logger.info("New Google user created: {}", email);
        }

        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "user", user
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        User user = userService.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + loginRequest.getEmail()));

        String token = jwtService.generateToken(user);
        logger.info("User logged in: {}", user.getEmail());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "user", user
        ));
    }
}
