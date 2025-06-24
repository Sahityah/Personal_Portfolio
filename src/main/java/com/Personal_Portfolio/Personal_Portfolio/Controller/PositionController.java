package com.Personal_Portfolio.Personal_Portfolio.Controller;


import com.Personal_Portfolio.Personal_Portfolio.DTO.PositionDto;
import com.Personal_Portfolio.Personal_Portfolio.Entity.Position;
import com.Personal_Portfolio.Personal_Portfolio.Entity.User;
import com.Personal_Portfolio.Personal_Portfolio.Service.PositionService;
import com.Personal_Portfolio.Personal_Portfolio.Service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/positions")
@CrossOrigin(origins = "*")
public class PositionController {

    private final PositionService positionService;
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(PositionController.class);

    public PositionController(PositionService positionService, UserService userService) {
        this.positionService = positionService;
        this.userService = userService;
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated.");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found."));
    }

    @GetMapping
    public ResponseEntity<Page<Position>> getAllPositions(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        try {
            User currentUser = getAuthenticatedUser();
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Position> positions = positionService.getPositionsByUser(currentUser, pageable);
            return ResponseEntity.ok(positions);
        } catch (Exception e) {
            logger.error("Error fetching positions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> addPosition(@Valid @RequestBody PositionDto request) {
        try {
            User currentUser = getAuthenticatedUser();
            Position position = new Position();
            position.setSymbol(request.getSymbol());
            position.setType(request.getType());
            position.setStrike(request.getStrike());
            position.setQty(request.getQty());
            position.setEntryPrice(request.getEntryPrice());
            position.setStatus(request.getStatus() != null ? request.getStatus() : "active");
            position.setSegment(positionService.determineSegment(request.getType()));
            Position saved = positionService.addPosition(position, currentUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            logger.error("Error adding position", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePosition(@PathVariable Long id, @RequestBody Position updatedPosition) {
        try {
            User currentUser = getAuthenticatedUser();
            Optional<Position> result = positionService.updatePosition(id, updatedPosition, currentUser);
            return result.<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            logger.error("Error updating position", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePosition(@PathVariable Long id) {
        try {
            User currentUser = getAuthenticatedUser();
            boolean deleted = positionService.deletePosition(id, currentUser);
            return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting position", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
