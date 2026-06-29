package com.library.mapper;

import com.library.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {
    Category findById(Integer id);
    List<Category> findAll();
    List<Category> findAllWithCount();
    void insert(Category category);
    void update(Category category);
    void deleteById(Integer id);
    long countBooksByCategory(Integer categoryId);
}