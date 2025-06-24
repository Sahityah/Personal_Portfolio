package com.Personal_Portfolio.Personal_Portfolio.Security;

import com.Personal_Portfolio.Personal_Portfolio.Service.JwtService;
import com.Personal_Portfolio.Personal_Portfolio.Service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections; // Import Collections for a single immutable list
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JwtAuthFilter.class);



    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/");
    }
    /**
     * This method filters incoming HTTP requests to validate JWT tokens.
     * If a valid JWT is found and the user is authenticated, it sets the authentication
     * context in Spring Security.
     *
     * @param request The HTTP request.
     * @param response The HTTP response.
     * @param filterChain The filter chain to proceed with.
     * @throws ServletException If a servlet-specific error occurs.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Extract the Authorization header from the request.
        final String authHeader = request.getHeader("Authorization");

        // If no Authorization header or it doesn't start with "Bearer ",
        // proceed to the next filter without authentication.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract the JWT token (remove "Bearer " prefix).
        final String jwt = authHeader.substring(7);
        final String userEmail;

        try {
            // Attempt to extract the username (email) from the JWT.
            userEmail = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            logger.warn("Invalid JWT Token: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid or expired JWT token.\"}");
            return;
        }

        // If userEmail is extracted and no authentication is currently set in the SecurityContextHolder.
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Retrieve user details from the UserService based on the email from the JWT.
            // Map the custom User entity to Spring Security's UserDetails.
            Optional<UserDetails> userDetailsOpt = userService.findByEmail(userEmail)
                    .map(u -> new org.springframework.security.core.userdetails.User(
                            u.getEmail(),          // Username (email)
                            u.getPassword() != null ? u.getPassword() : "", // Password (can be empty for OAuth2 users)
                            // Assign a default "USER" role as the 'role' field was removed from User entity.
                            // If you re-introduce roles, this logic will need to be updated.
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                    ));

            // If user details are found.
            if (userDetailsOpt.isPresent()) {
                UserDetails userDetails = userDetailsOpt.get();

                // Validate the JWT token against the retrieved user details.
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // Create an Authentication token for Spring Security.
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, // Principal (UserDetails object)
                                    null,        // Credentials (password, set to null after authentication)
                                    userDetails.getAuthorities() // User's authorities/roles
                            );

                    // Set the authentication token in the SecurityContextHolder.
                    // This tells Spring Security that the current user is authenticated.
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }

        // Proceed to the next filter in the chain.
        filterChain.doFilter(request, response);
    }
}
