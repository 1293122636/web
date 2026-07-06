package com.library.service;

import com.library.dto.response.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StatsServiceTest extends AbstractServiceTest {

    private StatsService statsService;

    @Mock
    private BookService bookService;

    @BeforeEach
    void setUp() {
        statsService = new StatsService(bookMapper, userMapper, borrowRecordMapper, categoryMapper, bookService);
    }

    @Test
    void getOverview_shouldReturnAllStats() {
        when(bookMapper.count()).thenReturn(20L);
        when(userMapper.countReaders()).thenReturn(8L);
        when(categoryMapper.findAll()).thenReturn(List.of(
                createCategory(1, "计算机"),
                createCategory(2, "文学"),
                createCategory(3, "经济"),
                createCategory(4, "科学")
        ));
        when(borrowRecordMapper.countActive()).thenReturn(10L);
        when(borrowRecordMapper.countOverdue()).thenReturn(2L);

        var result = statsService.getOverview();

        assertEquals(20, result.getTotalBooks());
        assertEquals(8, result.getTotalReaders());
        assertEquals(4, result.getTotalCategories());
        assertEquals(10, result.getActiveBorrows());
        assertEquals(2, result.getOverdueCount());
    }

    @Test
    void getOverview_shouldHandleZeroCounts() {
        when(bookMapper.count()).thenReturn(0L);
        when(userMapper.countReaders()).thenReturn(0L);
        when(categoryMapper.findAll()).thenReturn(List.of());
        when(borrowRecordMapper.countActive()).thenReturn(0L);
        when(borrowRecordMapper.countOverdue()).thenReturn(0L);

        var result = statsService.getOverview();

        assertEquals(0, result.getTotalBooks());
        assertEquals(0, result.getTotalReaders());
        assertEquals(0, result.getTotalCategories());
        assertEquals(0, result.getActiveBorrows());
        assertEquals(0, result.getOverdueCount());
    }

    @Test
    void getPopular_shouldReturnEnrichedBooks() {
        when(borrowRecordMapper.popularBooks()).thenReturn(List.of(popularRow(1, "Java", "A", "123", 1, 5)));
        when(categoryMapper.findById(1)).thenReturn(createCategory(1, "计算机"));

        var result = statsService.getPopular();

        assertEquals(1, result.size());
        assertEquals("Java", result.get(0).getTitle());
        assertEquals(5, ((Map<?, ?>) result.get(0).get_count()).get("borrowRecords"));
        assertEquals("计算机", result.get(0).getCategory().getName());
    }

    @Test
    void getPopular_shouldHandleNullCategory() {
        Map<String, Object> row = new HashMap<>();
        row.put("id", 1);
        row.put("title", "Book");
        row.put("author", "A");
        row.put("isbn", "123");
        row.put("categoryId", null);
        row.put("borrowCount", 1);
        when(borrowRecordMapper.popularBooks()).thenReturn(List.of(row));

        var result = statsService.getPopular();

        assertEquals(1, result.size());
        assertNull(result.get(0).getCategory());
    }

    @Test
    void getPopular_shouldHandleEmptyResult() {
        when(borrowRecordMapper.popularBooks()).thenReturn(List.of());
        assertTrue(statsService.getPopular().isEmpty());
    }

    @Test
    void getMonthly_shouldReturnMonthlyData() {
        List<Map<String, Object>> rawData = new ArrayList<>();
        rawData.add(monthRow("2026-01", 5L));
        rawData.add(monthRow("2026-02", 3L));
        when(borrowRecordMapper.monthlyStats(any())).thenReturn(rawData);

        var result = statsService.getMonthly();

        assertEquals(2, result.size());
        assertEquals("2026-01", result.get(0).getMonth());
        assertEquals(5L, result.get(0).getCount());
    }

    @Test
    void getMonthly_shouldHandleNullCount() {
        List<Map<String, Object>> rawData = new ArrayList<>();
        rawData.add(monthRow("2026-01", null));
        when(borrowRecordMapper.monthlyStats(any())).thenReturn(rawData);

        var result = statsService.getMonthly();
        assertEquals(1, result.size());
        assertEquals(0L, result.get(0).getCount());
    }

    @Test
    void getFacets_shouldDelegateToBookService() {
        Map<String, List<Map<String, Object>>> mockFacets = new HashMap<>();
        mockFacets.put("campus", List.of(Map.of("value", "校本部", "count", 10L)));
        when(bookService.getFacets(any())).thenReturn(Map.of("facets", mockFacets));

        var result = statsService.getFacets(Map.of("search", "test"));

        assertNotNull(result);
        assertNotNull(result.getFacets());
        assertTrue(result.getFacets().containsKey("campus"));
        verify(bookService).getFacets(any());
    }

    private com.library.entity.Category createCategory(Integer id, String name) {
        com.library.entity.Category c = new com.library.entity.Category();
        c.setId(id);
        c.setName(name);
        return c;
    }

    private Map<String, Object> popularRow(Object id, String title, String author, String isbn, Object catId, Object count) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", id);
        row.put("title", title);
        row.put("author", author);
        row.put("isbn", isbn);
        row.put("categoryId", catId);
        row.put("borrowCount", count);
        return row;
    }

    private Map<String, Object> monthRow(String month, Object count) {
        Map<String, Object> row = new HashMap<>();
        row.put("month", month);
        row.put("count", count);
        return row;
    }
}
