package com.library.mapper;

import com.library.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Integer id);

    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT * FROM users ORDER BY created_at DESC")
    List<User> findAll();

    @Insert("INSERT INTO users(username, password, name, role, phone, email, totalFines, patronCategoryId, created_at, updated_at) " +
            "VALUES(#{username}, #{password}, #{name}, #{role}, #{phone}, #{email}, 0, #{patronCategoryId}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    @Update("UPDATE users SET name=#{name}, phone=#{phone}, email=#{email}, updated_at=NOW() WHERE id=#{id}")
    void update(User user);

    @Update("UPDATE users SET total_fines = total_fines + #{amount} WHERE id=#{userId}")
    void addFine(@Param("userId") Integer userId, @Param("amount") java.math.BigDecimal amount);

    @Select("SELECT * FROM users ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<User> findPage(@Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM users")
    long count();
}
