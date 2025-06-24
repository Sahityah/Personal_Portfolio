package com.Personal_Portfolio.Personal_Portfolio.Controller;

import com.Personal_Portfolio.Personal_Portfolio.DTO.CandlestickDataDto;
import com.Personal_Portfolio.Personal_Portfolio.DTO.IndexDataDto;
import com.Personal_Portfolio.Personal_Portfolio.DTO.SecurityDto;
import com.Personal_Portfolio.Personal_Portfolio.Service.MarketDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    @Autowired
    private MarketDataService marketDataService;

    @GetMapping("/indices")
    public ResponseEntity<List<IndexDataDto>> getIndices() {
        // This endpoint can be public if you want to show indices on a public page,
        // or authenticated if only for logged-in users.
        // For now, let's keep it accessible for the Dashboard.
        return ResponseEntity.ok(marketDataService.getIndices());
    }

    @GetMapping("/chart-data/{symbol}")
    @PreAuthorize("isAuthenticated()") // Chart data typically requires authentication
    public ResponseEntity<List<CandlestickDataDto>> getChartData(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "1d") String interval) {
        return ResponseEntity.ok(marketDataService.getChartData(symbol, interval));
    }

    @GetMapping("/securities/search")
    public ResponseEntity<List<SecurityDto>> searchSecurities(@RequestParam String query) {
        // This can be public as it's for searching available securities
        return ResponseEntity.ok(marketDataService.searchSecurities(query));
    }
}
