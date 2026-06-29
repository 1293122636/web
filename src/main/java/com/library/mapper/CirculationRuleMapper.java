package com.library.mapper;

import com.library.entity.CirculationRule;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CirculationRuleMapper {
    CirculationRule findById(Integer id);
    List<CirculationRule> findAll();
    CirculationRule findByPatronAndItemType(@Param("patronCategoryId") Integer patronCategoryId, @Param("itemTypeId") Integer itemTypeId);
    CirculationRule findDefault();
    void upsert(CirculationRule rule);
}