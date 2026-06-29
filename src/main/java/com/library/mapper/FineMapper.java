package com.library.mapper;

import com.library.entity.Fine;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FineMapper {
    Fine findById(Integer id);
    List<Fine> findAll(@Param("type") String type, @Param("paid") Boolean paid);
    List<Fine> findByUserId(Integer userId);
    void insert(Fine fine);
    void markPaid(@Param("id") Integer id, @Param("paidAt") LocalDateTime paidAt);
}