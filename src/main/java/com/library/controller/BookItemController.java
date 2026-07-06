package com.library.controller;

import com.library.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/book-items")
public class BookItemController {

    private final BookService bookService;

    public BookItemController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/{barcode}")
    public ResponseEntity<?> lookupByBarcode(@PathVariable String barcode) {
        var result = bookService.lookupByBarcode(barcode);
        if (result == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }
}
