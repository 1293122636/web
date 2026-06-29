package com.library.controller;

import com.library.entity.Hold;
import com.library.service.HoldService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/holds")
public class HoldController {

    private final HoldService holdService;

    public HoldController(HoldService holdService) {
        this.holdService = holdService;
    }

    @PostMapping
    public ResponseEntity<?> createHold(HttpServletRequest request, @RequestBody Map<String, Integer> body) {
        Integer userId = (Integer) request.getAttribute("userId");
        Integer bookId = body.get("bookId");
        Hold hold = holdService.createHold(userId, bookId);
        return ResponseEntity.status(HttpStatus.CREATED).body(hold);
    }

    @GetMapping("/count")
    public ResponseEntity<?> count(@RequestParam Integer bookId) {
        long count = holdService.countPendingByBookId(bookId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyHolds(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return ResponseEntity.ok(holdService.getMyHolds(userId));
    }

    @GetMapping
    public ResponseEntity<?> listHolds(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer bookId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(holdService.listHolds(status, bookId, page, limit));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelHold(@PathVariable Integer id, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        holdService.cancelHold(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/fulfill")
    public ResponseEntity<?> fulfillHold(@PathVariable Integer id) {
        return ResponseEntity.ok(holdService.fulfillHold(id));
    }
}
