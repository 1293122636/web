package com.library.mapper;

import com.library.entity.Fine;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FineMapper {

    @Select("SELECT * FROM fines WHERE id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "user", column = "userId",
            one = @One(select = "com.library.mapper.UserMapper.findById")),
        @Result(property = "borrowRecord", column = "borrowRecordId",
            one = @One(select = "com.library.mapper.BorrowRecordMapper.findById"))
    })
    Fine findById(Integer id);

    @Select("<script>" +
            "SELECT * FROM fines WHERE 1=1 " +
            "<if test='type != null'>AND type = #{type}</if> " +
            "<if test='paid != null'>AND paid = #{paid}</if> " +
            "ORDER BY created_at DESC</script>")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "user", column = "userId",
            one = @One(select = "com.library.mapper.UserMapper.findById")),
        @Result(property = "borrowRecord", column = "borrowRecordId",
            one = @One(select = "com.library.mapper.BorrowRecordMapper.findById"))
    })
    List<Fine> findAll(@Param("type") String type, @Param("paid") Boolean paid);

    @Select("SELECT * FROM fines WHERE userId = #{userId} ORDER BY created_at DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "borrowRecord", column = "borrowRecordId",
            one = @One(select = "com.library.mapper.BorrowRecordMapper.findById"))
    })
    List<Fine> findByUserId(Integer userId);

    @Insert("INSERT INTO fines(amount, paid, paid_at, borrowRecordId, userId, type, created_at) " +
            "VALUES(#{amount}, false, NULL, #{borrowRecordId}, #{userId}, #{type}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Fine fine);

    @Update("UPDATE fines SET paid=true, paid_at=#{paidAt} WHERE id=#{id}")
    void markPaid(@Param("id") Integer id, @Param("paidAt") LocalDateTime paidAt);
}
