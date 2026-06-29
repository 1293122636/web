package com.library.controller;

import com.library.dto.response.StatsOverviewResponse;
import com.library.service.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping
    public ResponseEntity<StatsOverviewResponse> overview() {
        return ResponseEntity.ok(statsService.getOverview());
    }

    @GetMapping("/popular")
    public ResponseEntity<?> popular() {
        return ResponseEntity.ok(statsService.getPopular());
    }

    @GetMapping("/monthly")
    public ResponseEntity<?> monthly() {
        return ResponseEntity.ok(statsService.getMonthly());
    }
}
