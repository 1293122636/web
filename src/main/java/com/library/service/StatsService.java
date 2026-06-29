package com.library.service;

import com.library.dto.response.StatsOverviewResponse;
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
        stats.setTotalReaders(userMapper.count());
        stats.setTotalCategories(categoryMapper.findAll().size());
        // These need proper queries - simplified for now
        stats.setActiveBorrows(0);
        stats.setOverdueCount(0);
        return stats;
    }

    public List<Map<String, Object>> getPopular() {
        return borrowRecordMapper.popularBooks();
    }

    public List<Map<String, Object>> getMonthly() {
        LocalDateTime since = LocalDateTime.now().minusMonths(12);
        return borrowRecordMapper.monthlyStats(since);
    }

    public Map<String, Object> getFacets(Map<String, Object> params) {
        return Map.of("facets", Map.of());
    }
}
