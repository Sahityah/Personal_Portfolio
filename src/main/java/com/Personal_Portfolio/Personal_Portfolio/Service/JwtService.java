package com.Personal_Portfolio.Personal_Portfolio.Service;

import com.Personal_Portfolio.Personal_Portfolio.Config.GoogleOAuthConfig;
import com.Personal_Portfolio.Personal_Portfolio.Entity.User;
// import com.auth0.jwt.JWT; // No longer used for generation
// import com.auth0.jwt.algorithms.Algorithm; // No longer used for generation
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm; // For specifying HMAC SHA256
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap; // For building claims
import java.util.Map; // For building claims
import java.util.function.Function;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class); // Initialize logger

    private final GoogleOAuthConfig googleOAuthConfig;
    private final String jwtSecret;

    // JWT token expiration time in milliseconds (24 hours)
    private static final long JWT_EXPIRATION_MS = 1000 * 60 * 60 * 24;

    public JwtService(GoogleOAuthConfig googleOAuthConfig,
                      @Value("${jwt.secret}") String jwtSecret) {
        this.googleOAuthConfig = googleOAuthConfig;
        this.jwtSecret = jwtSecret;
    }

    /**
     * Generates a JWT token for the given user.
     * This method now uses io.jsonwebtoken (jjwt) for token creation,
     * ensuring consistency with token parsing and validation.
     *
     * @param user The user for whom to generate the token.
     * @return The generated JWT token string.
     */
    public String generateToken(User user) {
        // Prepare claims for the JWT
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("name", user.getUsername());
        claims.put("provider", user.getProvider().name()); // Store provider as a string

        // Build the JWT token using JJWT
        return Jwts.builder()
                .setClaims(claims) // Set custom claims
                .setSubject(user.getEmail()) // Set the subject (typically username/email)
                .setIssuedAt(new Date(System.currentTimeMillis())) // Set issued at time
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_MS)) // Set expiration time
                .signWith(getSignKey(), SignatureAlgorithm.HS256) // Sign with the secret key and HS256 algorithm
                .compact(); // Build and compact the JWT into a string
    }

    /**
     * Extracts the username (subject) from a JWT token.
     *
     * @param token The JWT token string.
     * @return The username (email) extracted from the token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from a JWT token.
     *
     * @param token The JWT token string.
     * @return The expiration Date extracted from the token.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from a JWT token using a resolver function.
     *
     * @param token The JWT token string.
     * @param claimsResolver A function to resolve the desired claim from the Claims object.
     * @param <T> The type of the claim to be extracted.
     * @return The extracted claim.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Validates if a JWT token is valid for a given UserDetails.
     * Checks if the username matches and if the token is not expired.
     *
     * @param token The JWT token string.
     * @param userDetails The UserDetails object to validate against.
     * @return true if the token is valid, false otherwise.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Checks if a JWT token has expired.
     *
     * @param token The JWT token string.
     * @return true if the token is expired, false otherwise.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts all claims from a JWT token.
     *
     * @param token The JWT token string.
     * @return The Claims object containing all claims from the token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey()) // Set the signing key for verification
                .build()
                .parseClaimsJws(token) // Parse the token and verify its signature
                .getBody(); // Get the claims body
    }

    /**
     * Retrieves the signing key from the JWT secret.
     *
     * @return The Key object used for signing and verifying JWTs.
     */
    private Key getSignKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Verifies a Google ID Token.
     * This method is used for authenticating users who sign in via Google.
     * Logs exceptions instead of just printing stack trace and returns null.
     *
     * @param token The Google ID Token string.
     * @return The payload of the Google ID Token if verification is successful, null otherwise.
     */
    public GoogleIdToken.Payload verifyGoogleToken(String token) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(), // Use trusted HTTP transport
                    JacksonFactory.getDefaultInstance() // Use Jackson for JSON parsing
            )
                    .setAudience(Collections.singletonList(googleOAuthConfig.getClientId())) // Set expected audience (your client ID)
                    .build();

            GoogleIdToken idToken = verifier.verify(token); // Verify the ID token
            if (idToken != null) {
                return idToken.getPayload(); // Return the payload if valid
            }
        } catch (Exception e) {
            // Log the exception instead of just printing stack trace for better production logging
            logger.error("Error verifying Google token: {}", e.getMessage(), e);
        }
        return null; // Return null if verification fails
    }
}
