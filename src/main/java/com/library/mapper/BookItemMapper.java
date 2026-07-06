package com.library.mapper;

import com.library.entity.BookItem;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface BookItemMapper {
    BookItem findById(Integer id);
    BookItem findByBarcode(String barcode);
    List<BookItem> findByBookId(Integer bookId);
    BookItem findFirstAvailableByBookId(Integer bookId);
    List<BookItem> findAvailableByBookId(Integer bookId);
    long countAvailableByBookId(Integer bookId);
    long countByBookId(Integer bookId);
    List<String> findCampuses();
    List<Map<String, Object>> countByCampus(Map<String, Object> params);
    List<Map<String, Object>> countByLocation(Map<String, Object> params);
    void insert(BookItem item);
    int updateStatus(@Param("id") Integer id, @Param("status") String status);
    void incrementRequests(Integer id);
}