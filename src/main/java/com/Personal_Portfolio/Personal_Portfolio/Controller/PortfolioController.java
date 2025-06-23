package com.Personal_Portfolio.Personal_Portfolio.Controller;

import com.Personal_Portfolio.Personal_Portfolio.DTO.TradeRequest;
import com.Personal_Portfolio.Personal_Portfolio.Entity.Portfolio;
import com.Personal_Portfolio.Personal_Portfolio.Entity.Position;
import com.Personal_Portfolio.Personal_Portfolio.Entity.Trade;
import com.Personal_Portfolio.Personal_Portfolio.Service.PortfolioService;
import com.Personal_Portfolio.Personal_Portfolio.Service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final TradeService tradeService;

    // Helper to get authenticated user's ID
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("User not authenticated.");
        }
        // Assuming your UserDetails service returns a User object with getId()
        // Or, if using JWT subject, you might need to query the user by username to get ID
        String username = authentication.getName(); // This is the username from the JWT subject
        // You'll need to fetch the User object to get the ID
        // For simplicity, let's assume username is also the ID for this example,
        // but in real app, fetch User from DB using username
        // For now, hardcoding an ID or converting username to ID if it's numeric
        // A better way: store user ID as a claim in JWT or query from UserDetailsService
        // For now, let's just make it a placeholder for the actual ID retrieval.
        // A real implementation would involve a User entity lookup or custom JWT claims.
        // Let's assume for now, the frontend sends the userId with request for simplicity in this example.
        // In a proper secure system, userId would be derived from the authenticated JWT.
        throw new UnsupportedOperationException("User ID retrieval from JWT not fully implemented. Pass userId for now.");
    }

    // Corrected helper to get current User ID from JWT context.
    // This assumes your JWT 'sub' claim is the username, and you resolve the ID from there.
    private Long getUserIdFromAuthentication(Authentication authentication) {
        // You would typically have a UserService that can get a User by username.
        // For simplicity, let's assume the username itself can be parsed as an ID if it's numeric,
        // or you would retrieve the user from the database and get their ID.
        // In a production app, the User entity would be loaded based on the 'sub' (username).
        // For this example, let's just use a dummy ID or expect it in path for demonstration.
        // A robust solution:
        // User user = userService.findByUsername(authentication.getName()).orElseThrow(...);
        // return user.getId();
        // As a temporary measure for demonstration:
        try {
            return Long.parseLong(authentication.getName()); // If your username IS the user ID (unlikely in real app)
        } catch (NumberFormatException e) {
            // Fallback for demo: use a default ID or fail
            System.err.println("Warning: Username cannot be parsed as ID. Using dummy ID 1 for demo.");
            return 1L; // Dummy ID for unhandled scenarios
        }
    }


    @GetMapping("/dashboard")
    public ResponseEntity<Portfolio> getDashboardData(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        Portfolio portfolio = portfolioService.getPortfolioByUserId(userId);
        return ResponseEntity.ok(portfolio);
    }

    @GetMapping("/positions")
    public ResponseEntity<List<Position>> getOpenPositions(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<Position> positions = portfolioService.getOpenPositionsByUserId(userId);
        return ResponseEntity.ok(positions);
    }

    @GetMapping("/trades")
    public ResponseEntity<List<Trade>> getTradeHistory(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<Trade> trades = tradeService.getTradesByUserId(userId);
        return ResponseEntity.ok(trades);
    }

    @PostMapping("/trade")
    public ResponseEntity<Trade> placeTrade(@RequestBody TradeRequest tradeRequest, Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        Trade trade = tradeService.requestTrade(
                userId,
                tradeRequest.getSymbol(),
                tradeRequest.getTradeType(),
                tradeRequest.getInstrumentType(),
                tradeRequest.getQuantity(),
                tradeRequest.getPrice(),
                tradeRequest.getStrikePrice(),
                tradeRequest.getExpiryDate()
        );
        return new ResponseEntity<>(trade, HttpStatus.ACCEPTED); // Accepted as it's asynchronous
    }
}