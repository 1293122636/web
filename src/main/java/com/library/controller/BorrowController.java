package com.library.controller;

import com.library.dto.request.BorrowRequest;
import com.library.entity.BorrowRecord;
import com.library.service.BorrowService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/borrows")
public class BorrowController {

    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyBorrows(HttpServletRequest request,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "20") int limit) {
        Integer userId = (Integer) request.getAttribute("userId");
        return ResponseEntity.ok(borrowService.getMyBorrows(userId, page, limit));
    }

    @GetMapping
    public ResponseEntity<?> listBorrows(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(borrowService.listBorrows(page, limit));
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(HttpServletRequest request,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "20") int limit) {
        Integer userId = (Integer) request.getAttribute("userId");
        return ResponseEntity.ok(borrowService.getHistory(userId, page, limit));
    }

    @PostMapping("/borrow")
    public ResponseEntity<?> borrow(HttpServletRequest request, @Valid @RequestBody BorrowRequest params) {
        Integer userId = (Integer) request.getAttribute("userId");
        BorrowRecord record = borrowService.borrow(userId, params);
        return ResponseEntity.status(HttpStatus.CREATED).body(record);
    }

    @PostMapping("/return/{id}")
    public ResponseEntity<?> returnBook(@PathVariable Integer id, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("userRole");
        boolean isAdmin = "admin".equals(role);
        return ResponseEntity.ok(borrowService.returnBook(id, userId, isAdmin));
    }

    @PostMapping("/renew/{id}")
    public ResponseEntity<?> renew(@PathVariable Integer id, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return ResponseEntity.ok(borrowService.renew(id, userId));
    }
}
