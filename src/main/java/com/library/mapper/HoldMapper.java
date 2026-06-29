package com.library.mapper;

import com.library.entity.Hold;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface HoldMapper {

    @Select("SELECT * FROM holds WHERE id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "book", column = "bookId",
            one = @One(select = "com.library.mapper.BookMapper.findById")),
        @Result(property = "user", column = "userId",
            one = @One(select = "com.library.mapper.UserMapper.findById")),
        @Result(property = "bookItem", column = "bookItemId",
            one = @One(select = "com.library.mapper.BookItemMapper.findById"))
    })
    Hold findById(Integer id);

    @Select("SELECT * FROM holds WHERE userId = #{userId} ORDER BY request_date DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "book", column = "bookId",
            one = @One(select = "com.library.mapper.BookMapper.findById"))
    })
    List<Hold> findByUserId(Integer userId);

    @Select("<script>" +
            "SELECT * FROM holds WHERE 1=1 " +
            "<if test='status != null'>AND status = #{status}</if> " +
            "<if test='bookId != null'>AND book_id = #{bookId}</if> " +
            "ORDER BY request_date DESC</script>")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "book", column = "bookId",
            one = @One(select = "com.library.mapper.BookMapper.findById")),
        @Result(property = "user", column = "userId",
            one = @One(select = "com.library.mapper.UserMapper.findById"))
    })
    List<Hold> findAll(@Param("status") String status, @Param("bookId") Integer bookId);

    @Select("SELECT * FROM holds WHERE bookId = #{bookId} AND status = 'pending' ORDER BY request_date ASC LIMIT 1")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "book", column = "bookId",
            one = @One(select = "com.library.mapper.BookMapper.findById"))
    })
    Hold findNextPendingByBookId(Integer bookId);

    @Select("SELECT * FROM holds WHERE userId = #{userId} AND book_id = #{bookId} AND status IN ('pending', 'ready') LIMIT 1")
    Hold findExistingHold(@Param("userId") Integer userId, @Param("bookId") Integer bookId);

    @Select("SELECT COUNT(*) FROM holds WHERE userId = #{userId} AND status IN ('pending', 'ready')")
    long countActiveByUserId(Integer userId);

    @Select("SELECT COUNT(*) FROM holds WHERE bookId = #{bookId} AND status = 'pending'")
    long countPendingByBookId(Integer bookId);

    @Select("SELECT * FROM holds WHERE status = 'ready' AND expiry_date IS NOT NULL AND expiry_date < NOW()")
    List<Hold> findExpiredReadyHolds();

    @Insert("INSERT INTO holds(userId, bookId, status, request_date, created_at) VALUES(#{userId}, #{bookId}, 'pending', NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Hold hold);

    @Update("UPDATE holds SET status=#{status}, bookItemId=#{bookItemId}, expiry_date=#{expiryDate} WHERE id=#{id}")
    void updateToReady(@Param("id") Integer id, @Param("bookItemId") Integer bookItemId, @Param("expiryDate") LocalDateTime expiryDate);

    @Update("UPDATE holds SET status=#{status}, fulfilled_at=NOW() WHERE id=#{id}")
    void fulfill(@Param("id") Integer id, @Param("status") String status);

    @Update("UPDATE holds SET status=#{status} WHERE id=#{id}")
    void updateStatus(@Param("id") Integer id, @Param("status") String status);

    @Delete("DELETE FROM holds WHERE id=#{id}")
    void deleteById(Integer id);
}
