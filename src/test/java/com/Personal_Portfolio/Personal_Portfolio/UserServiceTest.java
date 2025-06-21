package com.Personal_Portfolio.Personal_Portfolio;


import com.Personal_Portfolio.Personal_Portfolio.Entity.User;
import com.Personal_Portfolio.Personal_Portfolio.Repository.UserRepository;
import com.Personal_Portfolio.Personal_Portfolio.Service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser() {
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setPassword("password");

        when(passwordEncoder.encode(user.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(user)).thenReturn(user);

        User savedUser = userService.registerUser(user);

        assertNotNull(savedUser);
        assertEquals("encoded-password", savedUser.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testFindByEmail() {
        User user = new User();
        user.setEmail("test@gmail.com");

        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByEmail("test@gmail.com");

        assertTrue(result.isPresent());
        assertEquals("test@gmail.com", result.get().getEmail());
    }
}

