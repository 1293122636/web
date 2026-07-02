package com.library.mapper;

import com.library.entity.BorrowRecord;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface BorrowRecordMapper {
    BorrowRecord findById(Integer id);
    List<BorrowRecord> findByUserId(Integer userId);
    List<BorrowRecord> findActiveByUserId(Integer userId);
    BorrowRecord findActiveByUserAndBook(@Param("userId") Integer userId, @Param("bookId") Integer bookId);
    long countActiveByUserId(Integer userId);
    long countActiveByBookId(Integer bookId);
    List<BorrowRecord> findAll();
    List<BorrowRecord> findByUserIdPage(@Param("userId") Integer userId, @Param("offset") int offset, @Param("limit") int limit);
    List<BorrowRecord> findAllPage(@Param("offset") int offset, @Param("limit") int limit);
    long countByUserId(@Param("userId") Integer userId);
    long countAll();
    void insert(BorrowRecord record);
    void returnBook(@Param("id") Integer id, @Param("returnDate") LocalDateTime returnDate, @Param("status") String status);
    void renew(@Param("id") Integer id, @Param("dueDate") LocalDateTime dueDate);
    long countActive();
    long countOverdue();
    List<Map<String, Object>> monthlyStats(LocalDateTime since);
    List<Map<String, Object>> popularBooks();

    // For overdue fine scheduler
    List<BorrowRecord> findOverdueBorrows();

    // For circulation barcode scan
    BorrowRecord findActiveByBookItemId(@Param("bookItemId") Integer bookItemId);
}