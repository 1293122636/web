package com.library.mapper;

import com.library.entity.CirculationRule;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CirculationRuleMapper {

    @Select("SELECT * FROM circulation_rules WHERE id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "patronCategory", column = "patronCategoryId",
            one = @One(select = "com.library.mapper.PatronCategoryMapper.findById")),
        @Result(property = "itemType", column = "itemTypeId",
            one = @One(select = "com.library.mapper.ItemTypeMapper.findById"))
    })
    CirculationRule findById(Integer id);

    @Select("SELECT * FROM circulation_rules ORDER BY patronCategoryId, itemTypeId")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "patronCategory", column = "patronCategoryId",
            one = @One(select = "com.library.mapper.PatronCategoryMapper.findById")),
        @Result(property = "itemType", column = "itemTypeId",
            one = @One(select = "com.library.mapper.ItemTypeMapper.findById"))
    })
    List<CirculationRule> findAll();

    @Select("SELECT * FROM circulation_rules WHERE patronCategoryId = #{patronCategoryId} AND itemTypeId = #{itemTypeId} LIMIT 1")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "patronCategory", column = "patronCategoryId",
            one = @One(select = "com.library.mapper.PatronCategoryMapper.findById")),
        @Result(property = "itemType", column = "itemTypeId",
            one = @One(select = "com.library.mapper.ItemTypeMapper.findById"))
    })
    CirculationRule findByPatronAndItemType(@Param("patronCategoryId") Integer patronCategoryId, @Param("itemTypeId") Integer itemTypeId);

    @Select("SELECT * FROM circulation_rules WHERE patronCategoryId IS NULL AND itemTypeId IS NULL LIMIT 1")
    CirculationRule findDefault();

    @Insert("INSERT INTO circulation_rules(patronCategoryId, itemTypeId, maxBorrows, loanDays, renewals, renewalDays, finePerDay, created_at) " +
            "VALUES(#{patronCategoryId}, #{itemTypeId}, #{maxBorrows}, #{loanDays}, #{renewals}, #{renewalDays}, #{finePerDay}, NOW()) " +
            "ON DUPLICATE KEY UPDATE maxBorrows=#{maxBorrows}, loanDays=#{loanDays}, renewals=#{renewals}, renewalDays=#{renewalDays}, finePerDay=#{finePerDay}")
    void upsert(CirculationRule rule);
}
