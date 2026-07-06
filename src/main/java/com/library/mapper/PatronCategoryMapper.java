package com.library.mapper;

import com.library.entity.PatronCategory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PatronCategoryMapper {
    @Select("SELECT * FROM patron_categories WHERE id = #{id}")
    PatronCategory findById(Integer id);

    @Select("SELECT * FROM patron_categories ORDER BY name")
    List<PatronCategory> findAll();
}
