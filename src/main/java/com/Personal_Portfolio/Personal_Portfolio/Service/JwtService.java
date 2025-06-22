package com.Personal_Portfolio.Personal_Portfolio.Service;

import com.Personal_Portfolio.Personal_Portfolio.Config.GoogleOAuthConfig;
import com.Personal_Portfolio.Personal_Portfolio.Entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Collections;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import static java.security.KeyRep.Type.SECRET;

@Service
public class JwtService {

    @Autowired
    private GoogleOAuthConfig googleOAuthConfig;

    @Value("${jwt.secret}")
    private String jwtSecret;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(User user) {
        return JWT.create()
                .withSubject(user.getEmail())
                .withClaim("name", user.getName())
                .withClaim("id", user.getId())
                .withClaim("provider", user.getProvider().name()) // optional but helpful
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24 hours
                .sign(Algorithm.HMAC256(jwtSecret));
    }

    public GoogleIdToken.Payload verifyGoogleToken(String token) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance()
            )
                    .setAudience(Collections.singletonList(googleOAuthConfig.getClientId()))
                    .build();

            GoogleIdToken idToken = verifier.verify(token);
            if (idToken != null) {
                return idToken.getPayload();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}


