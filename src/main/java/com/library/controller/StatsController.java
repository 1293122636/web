package com.library.controller;

import com.library.dto.response.*;
import com.library.service.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<PopularBookDTO>> popular() {
        return ResponseEntity.ok(statsService.getPopular());
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlyStatsDTO>> monthly() {
        return ResponseEntity.ok(statsService.getMonthly());
    }
}
