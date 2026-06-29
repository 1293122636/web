package com.library.mapper;

import com.library.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {
    @Select("SELECT * FROM categories WHERE id = #{id}")
    Category findById(Integer id);

    @Select("SELECT * FROM categories ORDER BY name")
    List<Category> findAll();

    @Select("SELECT c.*, (SELECT COUNT(*) FROM books WHERE categoryId = c.id) as booksCount FROM categories c ORDER BY c.name")
    List<Category> findAllWithCount();

    @Insert("INSERT INTO categories(name, `desc`, created_at, updated_at) VALUES(#{name}, #{desc}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Category category);

    @Update("UPDATE categories SET name=#{name}, `desc`=#{desc}, updated_at=NOW() WHERE id=#{id}")
    void update(Category category);

    @Delete("DELETE FROM categories WHERE id=#{id}")
    void deleteById(Integer id);

    @Select("SELECT COUNT(*) FROM books WHERE categoryId = #{categoryId}")
    long countBooksByCategory(Integer categoryId);
}
