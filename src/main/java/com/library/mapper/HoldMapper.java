package com.library.mapper;

import com.library.entity.Hold;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface HoldMapper {
    Hold findById(Integer id);
    List<Hold> findByUserId(Integer userId);
    List<Hold> findAll(@Param("status") String status, @Param("bookId") Integer bookId);
    Hold findNextPendingByBookId(Integer bookId);
    Hold findExistingHold(@Param("userId") Integer userId, @Param("bookId") Integer bookId);
    long countActiveByUserId(Integer userId);
    long countPendingByBookId(Integer bookId);
    List<Hold> findExpiredReadyHolds();
    void insert(Hold hold);
    void updateToReady(@Param("id") Integer id, @Param("bookItemId") Integer bookItemId, @Param("expiryDate") LocalDateTime expiryDate);
    void fulfill(@Param("id") Integer id, @Param("status") String status);
    void updateStatus(@Param("id") Integer id, @Param("status") String status);
    void deleteById(Integer id);
}