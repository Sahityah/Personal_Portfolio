package com.Personal_Portfolio.Personal_Portfolio.Controller;

import com.Personal_Portfolio.Personal_Portfolio.DTO.ReportDto;
import com.Personal_Portfolio.Personal_Portfolio.Service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ReportDto> getUserReport(@PathVariable Long userId) {
        return ResponseEntity.ok(reportService.getUserReport(userId));
    }
}

