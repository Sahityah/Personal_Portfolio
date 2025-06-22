package com.Personal_Portfolio.Personal_Portfolio.Controller;

import com.Personal_Portfolio.Personal_Portfolio.DTO.GoogleLoginRequest;
import com.Personal_Portfolio.Personal_Portfolio.DTO.LoginRequest;
import com.Personal_Portfolio.Personal_Portfolio.DTO.RegisterRequest;
import com.Personal_Portfolio.Personal_Portfolio.Entity.User;
import com.Personal_Portfolio.Personal_Portfolio.Service.JwtService;
import com.Personal_Portfolio.Personal_Portfolio.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        User user = userService.register(request);
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok().body(token);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userService.login(request);
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok().body(token);
    }

    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequest request) {
        User user = userService.googleLogin(request);
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok().body(token);
    }
}
