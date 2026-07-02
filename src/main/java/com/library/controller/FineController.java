package com.library.controller;

import com.library.service.FineService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fines")
public class FineController {

    private final FineService fineService;

    public FineController(FineService fineService) {
        this.fineService = fineService;
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String type,
                                   @RequestParam(required = false) Boolean paid) {
        return ResponseEntity.ok(fineService.findAll(type, paid));
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyFines(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return ResponseEntity.ok(fineService.findByUserId(userId));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<?> pay(@PathVariable Integer id, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return ResponseEntity.ok(fineService.markPaid(id, userId));
    }
}
