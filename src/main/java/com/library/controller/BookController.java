package com.library.controller;

import com.library.dto.request.BookCreateRequest;
import com.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String campus,
            @RequestParam(required = false) Integer yearMin,
            @RequestParam(required = false) Integer yearMax,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {

        Map<String, Object> params = new java.util.HashMap<>();
        if (search != null) params.put("search", search);
        if (categoryId != null) params.put("categoryId", categoryId);
        if (campus != null) params.put("campus", campus);
        if (yearMin != null) params.put("yearMin", yearMin);
        if (yearMax != null) params.put("yearMax", yearMax);
        if (language != null) params.put("language", language);
        if (sortBy != null) params.put("sortBy", sortBy);
        params.put("page", page);
        params.put("limit", limit);

        return ResponseEntity.ok(bookService.list(params));
    }

    @GetMapping("/facets")
    public ResponseEntity<?> getFacets(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer categoryId) {
        Map<String, Object> params = new java.util.HashMap<>();
        if (search != null) params.put("search", search);
        if (categoryId != null) params.put("categoryId", categoryId);
        return ResponseEntity.ok(bookService.getFacets(params));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(bookService.getById(id));
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<?> getItems(@PathVariable Integer id) {
        return ResponseEntity.ok(bookService.getItemsByBookId(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody BookCreateRequest data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.create(data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        return ResponseEntity.ok(bookService.update(id, data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        bookService.remove(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reconcile")
    public ResponseEntity<?> reconcile(@PathVariable Integer id) {
        return ResponseEntity.ok(Map.of("message", "Reconciliation not implemented in MyBatis version"));
    }
}
