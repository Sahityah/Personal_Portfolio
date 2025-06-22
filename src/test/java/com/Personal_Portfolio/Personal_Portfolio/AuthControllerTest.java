/*package com.Personal_Portfolio.Personal_Portfolio;

import com.Personal_Portfolio.Personal_Portfolio.Controller.AuthController;
import com.Personal_Portfolio.Personal_Portfolio.Entity.User;
import com.Personal_Portfolio.Personal_Portfolio.Service.JwtService;
import com.Personal_Portfolio.Personal_Portfolio.Service.UserService;
import com.Personal_Portfolio.Personal_Portfolio.Config.JwtResponse;
import com.Personal_Portfolio.Personal_Portfolio.Config.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser() {
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setPassword("password");

        when(userService.registerUser(user)).thenReturn(user);

        ResponseEntity<?> response = authController.registerUser(user);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(userService, times(1)).registerUser(user);
    }

    @Test
    void testLoginUser() {
        LoginRequest loginRequest = new LoginRequest("test@gmail.com", "password");
        User user = new User();
        user.setEmail("test@gmail.com");

        when(userService.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("test-jwt-token");

        ResponseEntity<?> response = authController.loginUser(loginRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        JwtResponse jwtResponse = (JwtResponse) response.getBody();
        assertEquals("test-jwt-token", jwtResponse.getToken());
    }
}

