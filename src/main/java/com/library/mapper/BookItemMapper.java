package com.library.mapper;

import com.library.entity.BookItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

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
    void insert(BookItem item);
    int updateStatus(@Param("id") Integer id, @Param("status") String status);
    void incrementRequests(Integer id);
}