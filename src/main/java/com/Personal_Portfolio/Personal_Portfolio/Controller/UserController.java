package com.Personal_Portfolio.Personal_Portfolio.Controller;


import com.Personal_Portfolio.Personal_Portfolio.DTO.PasswordChangeRequest;
import com.Personal_Portfolio.Personal_Portfolio.DTO.UserDto;
import com.Personal_Portfolio.Personal_Portfolio.Entity.User;
import com.Personal_Portfolio.Personal_Portfolio.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@PreAuthorize("isAuthenticated()") // All methods in this controller require authentication
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        try {
            User currentUser = userService.getCurrentUser();
            UserDto userProfile = userService.getUserProfile(currentUser.getEmail());
            return ResponseEntity.ok(userProfile);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch user profile: " + e.getMessage());
        }
    }

    @PatchMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@Valid @RequestBody UserDto userDto) {
        try {
            User currentUser = userService.getCurrentUser();
            UserDto updatedProfile = userService.updateUserProfile(currentUser.getEmail(), userDto);
            return ResponseEntity.ok(updatedProfile);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update user profile: " + e.getMessage());
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        try {
            User currentUser = userService.getCurrentUser();
            userService.changePassword(currentUser.getEmail(), request);
            return ResponseEntity.ok("Password updated successfully!");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to change password: " + e.getMessage());
        }
    }
}
