package com.library.mapper;

import com.library.entity.BorrowRecord;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface BorrowRecordMapper {

    @Select("SELECT * FROM borrow_records WHERE id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "book", column = "bookId",
            one = @One(select = "com.library.mapper.BookMapper.findById")),
        @Result(property = "bookItem", column = "bookItemId",
            one = @One(select = "com.library.mapper.BookItemMapper.findById")),
        @Result(property = "user", column = "userId",
            one = @One(select = "com.library.mapper.UserMapper.findById"))
    })
    BorrowRecord findById(Integer id);

    @Select("SELECT * FROM borrow_records WHERE userId = #{userId} ORDER BY borrow_date DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "book", column = "bookId",
            one = @One(select = "com.library.mapper.BookMapper.findById")),
        @Result(property = "bookItem", column = "bookItemId",
            one = @One(select = "com.library.mapper.BookItemMapper.findById"))
    })
    List<BorrowRecord> findByUserId(Integer userId);

    @Select("SELECT * FROM borrow_records WHERE userId = #{userId} AND status = 'active'")
    List<BorrowRecord> findActiveByUserId(Integer userId);

    @Select("SELECT * FROM borrow_records WHERE userId = #{userId} AND book_id = #{bookId} AND status = 'active' LIMIT 1")
    BorrowRecord findActiveByUserAndBook(@Param("userId") Integer userId, @Param("bookId") Integer bookId);

    @Select("SELECT COUNT(*) FROM borrow_records WHERE userId = #{userId} AND status = 'active'")
    long countActiveByUserId(Integer userId);

    @Select("SELECT COUNT(*) FROM borrow_records WHERE bookId = #{bookId} AND status = 'active'")
    long countActiveByBookId(Integer bookId);

    @Select("SELECT * FROM borrow_records ORDER BY borrow_date DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "book", column = "bookId",
            one = @One(select = "com.library.mapper.BookMapper.findById")),
        @Result(property = "bookItem", column = "bookItemId",
            one = @One(select = "com.library.mapper.BookItemMapper.findById")),
        @Result(property = "user", column = "userId",
            one = @One(select = "com.library.mapper.UserMapper.findById"))
    })
    List<BorrowRecord> findAll();

    @Insert("INSERT INTO borrow_records(userId, bookId, bookItemId, borrow_date, due_date, status, created_at, updated_at) " +
            "VALUES(#{userId}, #{bookId}, #{bookItemId}, #{borrowDate}, #{dueDate}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(BorrowRecord record);

    @Update("UPDATE borrow_records SET return_date=#{returnDate}, status=#{status}, updated_at=NOW() WHERE id=#{id}")
    void returnBook(@Param("id") Integer id, @Param("returnDate") LocalDateTime returnDate, @Param("status") String status);

    @Update("UPDATE borrow_records SET due_date=#{dueDate}, renewed=true, updated_at=NOW() WHERE id=#{id}")
    void renew(@Param("id") Integer id, @Param("dueDate") LocalDateTime dueDate);

    @Select("SELECT DATE_FORMAT(borrow_date, '%Y-%m') as month, COUNT(*) as count FROM borrow_records " +
            "WHERE borrow_date >= #{since} GROUP BY DATE_FORMAT(borrow_date, '%Y-%m') ORDER BY month")
    List<Map<String, Object>> monthlyStats(LocalDateTime since);

    @Select("SELECT book_id, COUNT(*) as count FROM borrow_records GROUP BY book_id ORDER BY count DESC LIMIT 20")
    List<Map<String, Object>> popularBooks();
}
