package com.Personal_Portfolio.Personal_Portfolio.Controller;


import com.Personal_Portfolio.Personal_Portfolio.Entity.Trade;
import com.Personal_Portfolio.Personal_Portfolio.Service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @PostMapping("/add")
    public ResponseEntity<Trade> addTrade(@RequestBody Trade trade) {
        return ResponseEntity.ok(tradeService.addTrade(trade));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Trade>> getTradesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(tradeService.getTradesByUser(userId));
    }
}
