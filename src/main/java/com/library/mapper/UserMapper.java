package com.library.mapper;

import com.library.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    User findById(Integer id);
    User findByUsername(String username);
    List<User> findAll();
    void insert(User user);
    void update(User user);
    void addFine(@Param("userId") Integer userId, @Param("amount") java.math.BigDecimal amount);
    List<User> findPage(@Param("offset") int offset, @Param("limit") int limit);
    long countReaders();
    long count();
}