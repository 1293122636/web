package com.library.service;

import com.library.dto.response.CategoryResponse;
import com.library.entity.Category;
import com.library.exception.AppException;
import com.library.mapper.CategoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public List<CategoryResponse> findAll() {
        return categoryMapper.findAll().stream()
                .map(c -> {
                    long count = categoryMapper.countBooksByCategory(c.getId());
                    return new CategoryResponse(c.getId(), c.getName(), c.getDesc(), (int) count);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public Category create(String name, String desc) {
        Category category = new Category();
        category.setName(name);
        category.setDesc(desc);
        categoryMapper.insert(category);
        return category;
    }

    @Transactional
    public Category update(Integer id, String name, String desc) {
        Category category = categoryMapper.findById(id);
        if (category == null) throw AppException.notFound("Category not found");
        category.setName(name);
        category.setDesc(desc);
        categoryMapper.update(category);
        return categoryMapper.findById(id);
    }

    @Transactional
    public void delete(Integer id) {
        long bookCount = categoryMapper.countBooksByCategory(id);
        if (bookCount > 0) {
            throw AppException.badRequest("Cannot delete category with " + bookCount + " books");
        }
        categoryMapper.deleteById(id);
    }
}
