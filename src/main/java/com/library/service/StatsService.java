package com.library.service;

import com.library.dto.response.*;
import com.library.mapper.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class StatsService {

    private final BookMapper bookMapper;
    private final UserMapper userMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final CategoryMapper categoryMapper;

    public StatsService(BookMapper bookMapper, UserMapper userMapper,
                        BorrowRecordMapper borrowRecordMapper, CategoryMapper categoryMapper) {
        this.bookMapper = bookMapper;
        this.userMapper = userMapper;
        this.borrowRecordMapper = borrowRecordMapper;
        this.categoryMapper = categoryMapper;
    }

    public StatsOverviewResponse getOverview() {
        StatsOverviewResponse stats = new StatsOverviewResponse();
        stats.setTotalBooks(bookMapper.count());
        stats.setTotalReaders(userMapper.countReaders());
        stats.setTotalCategories(categoryMapper.findAll().size());
        stats.setActiveBorrows((int) borrowRecordMapper.countActive());
        stats.setOverdueCount((int) borrowRecordMapper.countOverdue());
        return stats;
    }

    public List<PopularBookDTO> getPopular() {
        List<Map<String, Object>> raw = borrowRecordMapper.popularBooks();
        List<PopularBookDTO> result = new ArrayList<>();
        for (Map<String, Object> row : raw) {
            PopularBookDTO dto = new PopularBookDTO();
            dto.setId(toInt(row.get("id")));
            dto.setTitle((String) row.get("title"));
            dto.setAuthor((String) row.get("author"));
            dto.setIsbn((String) row.get("isbn"));
            dto.setCategoryId(toInt(row.get("categoryId")));
            // Transform borrowCount to _count.borrowRecords
            Object bc = row.get("borrowCount");
            dto.set_count(Map.of("borrowRecords", bc != null ? bc : 0));
            // Enrich with category name
            Object catId = row.get("categoryId");
            if (catId instanceof Number) {
                var cat = categoryMapper.findById(((Number) catId).intValue());
                dto.setCategory(cat != null ? new CategoryResponse(cat.getId(), cat.getName(), cat.getDesc(), 0) : null);
            }
            result.add(dto);
        }
        return result;
    }

    public List<MonthlyStatsDTO> getMonthly() {
        LocalDateTime since = LocalDateTime.now().minusMonths(12);
        List<Map<String, Object>> raw = borrowRecordMapper.monthlyStats(since);
        List<MonthlyStatsDTO> result = new ArrayList<>();
        for (Map<String, Object> row : raw) {
            MonthlyStatsDTO dto = new MonthlyStatsDTO();
            dto.setMonth((String) row.get("month"));
            Object count = row.get("count");
            dto.setCount(count instanceof Number ? ((Number) count).longValue() : 0L);
            result.add(dto);
        }
        return result;
    }

    public FacetsDTO getFacets(Map<String, Object> params) {
        FacetsDTO dto = new FacetsDTO();
        dto.setFacets(Map.of());
        return dto;
    }

    private Integer toInt(Object value) {
        if (value instanceof Number) return ((Number) value).intValue();
        return null;
    }
}
