package com.library.mapper;

import com.library.entity.Book;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface BookMapper {
    Book findById(Integer id);
    Book findByIsbn(String isbn);
    List<Book> searchBooks(Map<String, Object> params);
    long countBooks(Map<String, Object> params);
    void insert(Book book);
    void update(Book book);
    void updateTotalAndAvailable(@Param("id") Integer id, @Param("total") Integer total, @Param("available") Integer available);
    int decrementAvailable(Integer id);
    int incrementAvailable(Integer id);
    void updateStatus(@Param("id") Integer id, @Param("status") String status);
    void deleteById(Integer id);
    long count();
    List<Book> findAll();
    List<String> findLanguages();
    List<Integer> findYears();
    List<Map<String, Object>> countByLanguage(Map<String, Object> params);
    List<Map<String, Object>> countByCategory(Map<String, Object> params);
    List<Map<String, Object>> countByYearDecade(Map<String, Object> params);
}