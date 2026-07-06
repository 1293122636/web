package com.library.mapper;

import com.library.entity.ItemType;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ItemTypeMapper {
    @Select("SELECT * FROM item_types WHERE id = #{id}")
    ItemType findById(Integer id);

    @Select("SELECT * FROM item_types ORDER BY name")
    List<ItemType> findAll();
}
