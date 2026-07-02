package com.library.service;

import com.library.dto.request.BookCreateRequest;
import com.library.entity.*;
import com.library.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BookServiceTest extends AbstractServiceTest {

    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookService = new BookService(bookMapper, bookItemMapper, categoryMapper, borrowRecordMapper, auditLogMapper);
    }

    @Test
    void list_shouldReturnPagedBooks() {
        Map<String, Object> params = new HashMap<>();
        params.put("search", "test");
        params.put("page", 1);
        params.put("limit", 20);

        Book book = new Book();
        book.setId(1);
        book.setTitle("Test Book");
        book.setAuthor("Author");
        book.setIsbn("978-0-123456-47-2");

        when(bookMapper.searchBooks(any())).thenReturn(List.of(book));
        when(bookMapper.countBooks(any())).thenReturn(1L);

        var result = bookService.list(params);
        assertEquals(1, ((List<?>) result.get("books")).size());
        assertEquals(1L, result.get("total"));
    }

    @Test
    void list_shouldEnforcePaginationBounds() {
        Map<String, Object> params = new HashMap<>();
        params.put("page", 0);
        params.put("limit", 100);

        when(bookMapper.searchBooks(any())).thenReturn(List.of());
        when(bookMapper.countBooks(any())).thenReturn(0L);

        var result = bookService.list(params);
        assertEquals(0L, result.get("total"));
    }

    @Test
    void getById_shouldReturnBookDetail() {
        Book book = new Book();
        book.setId(1);
        book.setTitle("Test");
        book.setAuthor("Author");
        book.setIsbn("978-0-123456-47-2");
        book.setCategory(new Category());

        when(bookMapper.findById(1)).thenReturn(book);
        when(bookItemMapper.findByBookId(1)).thenReturn(List.of());
        when(bookItemMapper.countByBookId(1)).thenReturn(0L);

        var detail = bookService.getById(1);
        assertEquals("Test", detail.getTitle());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(bookMapper.findById(999)).thenReturn(null);
        assertThrows(AppException.class, () -> bookService.getById(999));
    }

    @Test
    void create_shouldInsertBook() {
        BookCreateRequest req = new BookCreateRequest();
        req.setIsbn("978-0-123456-47-2");
        req.setTitle("New Book");
        req.setAuthor("Author");
        req.setTotal(3);
        req.setCategoryId(1);

        doAnswer(inv -> {
            Book b = inv.getArgument(0);
            b.setId(1);
            return null;
        }).when(bookMapper).insert(any(Book.class));

        Book created = bookService.create(req);
        assertNotNull(created);
        assertEquals("New Book", created.getTitle());
        verify(bookMapper).insert(any(Book.class));
    }

    @Test
    void create_shouldSetAvailableEqualToTotal() {
        BookCreateRequest req = new BookCreateRequest();
        req.setIsbn("978-0-123456-47-3");
        req.setTitle("Book");
        req.setAuthor("A");
        req.setTotal(5);
        req.setCategoryId(1);

        doAnswer(inv -> {
            Book b = inv.getArgument(0);
            b.setId(2);
            return null;
        }).when(bookMapper).insert(any(Book.class));

        Book created = bookService.create(req);
        assertEquals(5, created.getAvailable().intValue());
    }

    @Test
    void remove_shouldDeleteWhenNoCopies() {
        when(bookItemMapper.countByBookId(1)).thenReturn(0L);
        bookService.remove(1);
        verify(bookMapper).deleteById(1);
    }

    @Test
    void remove_shouldThrowWhenCopiesExist() {
        when(bookItemMapper.countByBookId(1)).thenReturn(3L);
        assertThrows(AppException.class, () -> bookService.remove(1));
        verify(bookMapper, never()).deleteById(any());
    }

    @Test
    void validateItemStatus_shouldAllowValidTransition() {
        assertDoesNotThrow(() -> bookService.validateItemStatus("available", "borrowed"));
    }

    @Test
    void validateItemStatus_shouldThrowForInvalidTransition() {
        assertThrows(AppException.class, () -> bookService.validateItemStatus("available", "expired"));
    }

    @Test
    void getFacets_shouldReturnFiveDimensions() {
        Map<String, Object> params = new HashMap<>();
        params.put("search", "test");

        // Mock all 5 facet query responses
        when(bookItemMapper.countByCampus(params)).thenReturn(List.of(
                Map.of("value", "校本部", "count", 10L),
                Map.of("value", "中心校区", "count", 5L)
        ));
        when(bookItemMapper.countByLocation(params)).thenReturn(List.of(
                Map.of("value", "一层社科库", "count", 8L)
        ));
        when(bookMapper.countByLanguage(params)).thenReturn(List.of(
                Map.of("value", "中文", "count", 15L)
        ));
        when(bookMapper.countByCategory(params)).thenReturn(List.of(
                Map.of("value", "计算机", "count", 7L)
        ));
        when(bookMapper.countByYearDecade(params)).thenReturn(List.of(
                Map.of("value", "2020s", "count", 12L)
        ));

        var result = bookService.getFacets(params);

        assertNotNull(result);
        assertTrue(result.containsKey("facets"));
        @SuppressWarnings("unchecked")
        Map<String, List<Map<String, Object>>> facets =
                (Map<String, List<Map<String, Object>>>) result.get("facets");

        // Verify all 5 dimensions present
        assertEquals(5, facets.size());
        assertTrue(facets.containsKey("campus"));
        assertTrue(facets.containsKey("location"));
        assertTrue(facets.containsKey("language"));
        assertTrue(facets.containsKey("subject"));
        assertTrue(facets.containsKey("yearRange"));

        // Verify campus facet values
        assertEquals(2, facets.get("campus").size());
        assertEquals("校本部", facets.get("campus").get(0).get("value"));
        assertEquals(10L, facets.get("campus").get(0).get("count"));

        // Verify all mappers were called once
        verify(bookItemMapper).countByCampus(params);
        verify(bookItemMapper).countByLocation(params);
        verify(bookMapper).countByLanguage(params);
        verify(bookMapper).countByCategory(params);
        verify(bookMapper).countByYearDecade(params);
    }

    @Test
    void getFacets_shouldPassFilterParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("search", "python");
        params.put("categoryId", 2);
        params.put("language", "中文");
        params.put("yearMin", 2020);
        params.put("yearMax", 2024);
        params.put("campus", "校本部");
        params.put("location", "一层社科库");

        when(bookItemMapper.countByCampus(params)).thenReturn(List.of());
        when(bookItemMapper.countByLocation(params)).thenReturn(List.of());
        when(bookMapper.countByLanguage(params)).thenReturn(List.of());
        when(bookMapper.countByCategory(params)).thenReturn(List.of());
        when(bookMapper.countByYearDecade(params)).thenReturn(List.of());

        var result = bookService.getFacets(params);

        assertNotNull(result);
        // Verify full params (including book-level filters) passed to campus/location
        verify(bookItemMapper).countByCampus(params);
        verify(bookItemMapper).countByLocation(params);
        verify(bookMapper).countByLanguage(params);
        verify(bookMapper).countByCategory(params);
        verify(bookMapper).countByYearDecade(params);
    }
}
