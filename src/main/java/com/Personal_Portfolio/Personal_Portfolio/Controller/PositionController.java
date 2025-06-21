package com.Personal_Portfolio.Personal_Portfolio.Controller;

import com.Personal_Portfolio.Personal_Portfolio.Entity.Position;
import com.Personal_Portfolio.Personal_Portfolio.Service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @PostMapping("/add")
    public ResponseEntity<Position> addPosition(@RequestBody Position position) {
        return ResponseEntity.ok(positionService.addPosition(position));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Position>> getPositionsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(positionService.getPositionsByUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Position> getPosition(@PathVariable Long id) {
        return positionService.getPosition(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePosition(@PathVariable Long id) {
        positionService.deletePosition(id);
        return ResponseEntity.ok().build();
    }
}

