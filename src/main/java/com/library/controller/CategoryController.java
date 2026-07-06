package com.library.controller;

import com.library.entity.Category;
import com.library.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(categoryService.findAll());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateCategoryRequest data) {
        Category cat = categoryService.create(data.getName(), data.getDesc());
        return ResponseEntity.status(HttpStatus.CREATED).body(cat);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody CreateCategoryRequest data) {
        return ResponseEntity.ok(categoryService.update(id, data.getName(), data.getDesc()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

@Data
class CreateCategoryRequest {
    @NotBlank
    private String name;
    private String desc;
}
