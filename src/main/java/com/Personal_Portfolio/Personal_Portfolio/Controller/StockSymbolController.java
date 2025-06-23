package com.Personal_Portfolio.Personal_Portfolio.Controller;

import com.Personal_Portfolio.Personal_Portfolio.Entity.StockSymbol;
import com.Personal_Portfolio.Personal_Portfolio.Service.StockSymbolService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/symbols")
@RequiredArgsConstructor
public class StockSymbolController {

    @Autowired
    private final StockSymbolService stockSymbolService;

    @GetMapping("/search")
    public ResponseEntity<List<StockSymbol>> searchStockSymbols(@RequestParam String query) {
        if (query.length() < 1) { // Require at least 1 character for search
            return ResponseEntity.ok(List.of()); // Return empty list for short queries
        }
        List<StockSymbol> matchingSymbols = stockSymbolService.searchSymbols(query);
        return ResponseEntity.ok(matchingSymbols);
    }
}
