package com.Personal_Portfolio.Personal_Portfolio.Controller;


import com.Personal_Portfolio.Personal_Portfolio.DTO.PnLChartDataDto;
import com.Personal_Portfolio.Personal_Portfolio.DTO.PortfolioDataDto;
import com.Personal_Portfolio.Personal_Portfolio.DTO.PositionDto;
import com.Personal_Portfolio.Personal_Portfolio.Entity.User;
import com.Personal_Portfolio.Personal_Portfolio.Service.PortfolioService;
import com.Personal_Portfolio.Personal_Portfolio.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/portfolio")
@PreAuthorize("isAuthenticated()") // All methods in this controller require authentication
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;
    @Autowired
    private UserService userService; // To get current authenticated user

    @GetMapping("getPortfolioOverview")
    public ResponseEntity<PortfolioDataDto> getPortfolioOverview() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        PortfolioDataDto portfolioData = portfolioService.getPortfolioData(currentUser);
        return ResponseEntity.ok(portfolioData);
    }

    @GetMapping("/getAllPositions")
    public ResponseEntity<List<PositionDto>> getAllPositions(
            @RequestParam Optional<String> status,
            @RequestParam Optional<String> segment) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<PositionDto> positions = portfolioService.getPositions(currentUser, status, segment);
        return ResponseEntity.ok(positions);
    }

    @PostMapping("/addPositions")
    public ResponseEntity<PositionDto> addPosition(@Valid @RequestBody PositionDto positionDto) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        PositionDto newPosition = portfolioService.addPosition(currentUser, positionDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newPosition);
    }

    @DeleteMapping("/positions/{id}")
    public ResponseEntity<Void> deletePosition(@PathVariable String id) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            portfolioService.deletePosition(currentUser, id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/pnl-chart-data")
    public ResponseEntity<PnLChartDataDto> getPnLChartData() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        PnLChartDataDto chartData = portfolioService.getPnLChartData(currentUser);
        return ResponseEntity.ok(chartData);
    }

}