package com.Personal_Portfolio.Personal_Portfolio.Service;

import com.Personal_Portfolio.Personal_Portfolio.Entity.User;
import com.Personal_Portfolio.Personal_Portfolio.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections; // Import Collections for a single immutable list
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Locates the user based on the username (email in this case).
     * This method is used by Spring Security's AuthenticationProvider to load user details
     * during the authentication process (e.g., for form-based login).
     *
     * @param email The email address of the user.
     * @return A UserDetails object representing the authenticated user.
     * @throws UsernameNotFoundException if the user could not be found.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Find the user by email in the database.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Return a Spring Security UserDetails object.
        // The password field from your User entity is used here.
        // For OAuth2 users, the password might be null, which Spring Security handles.
        // Assign a default "USER" role as the 'role' field was removed from User entity.
        // If you decide to re-introduce roles in your User entity, this logic will need to be updated
        // to retrieve the actual roles from the user object.
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword() != null ? user.getPassword() : "", // Provide an empty string if password is null (e.g., for OAuth2 users)
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) // Assign default ROLE_USER
        );
    }
}
