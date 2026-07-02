package com.library.service;

import com.library.entity.*;
import com.library.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HoldServiceTest extends AbstractServiceTest {

    private HoldService holdService;
    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookService = new BookService(bookMapper, bookItemMapper, categoryMapper, borrowRecordMapper, auditLogMapper);
        holdService = new HoldService(holdMapper, bookMapper, bookItemMapper, bookService, auditLogMapper);
    }

    @Test
    void createHold_shouldCreateWhenNoAvailableCopies() {
        Book book = new Book();
        book.setId(1);
        book.setAvailable(0);

        when(bookMapper.findById(1)).thenReturn(book);
        when(holdMapper.findExistingHold(1, 1)).thenReturn(null);
        when(holdMapper.countActiveByUserId(1)).thenReturn(1L);

        doAnswer(inv -> {
            Hold h = inv.getArgument(0);
            h.setId(100);
            return null;
        }).when(holdMapper).insert(any(Hold.class));

        Hold saved = new Hold();
        saved.setId(100);
        saved.setUserId(1);
        saved.setBookId(1);
        saved.setStatus("pending");
        when(holdMapper.findById(100)).thenReturn(saved);

        Hold result = holdService.createHold(1, 1);
        assertNotNull(result);
        assertEquals(100, result.getId());
        verify(holdMapper).insert(any(Hold.class));
    }

    @Test
    void createHold_shouldRejectWhenCopiesAvailable() {
        Book book = new Book();
        book.setId(1);
        book.setAvailable(3);

        when(bookMapper.findById(1)).thenReturn(book);
        assertThrows(AppException.class, () -> holdService.createHold(1, 1));
    }

    @Test
    void createHold_shouldRejectDuplicate() {
        Book book = new Book();
        book.setId(1);
        book.setAvailable(0);

        when(bookMapper.findById(1)).thenReturn(book);
        when(holdMapper.findExistingHold(1, 1)).thenReturn(new Hold());

        assertThrows(AppException.class, () -> holdService.createHold(1, 1));
    }

    @Test
    void createHold_shouldRejectWhenExceedingMax() {
        Book book = new Book();
        book.setId(1);
        book.setAvailable(0);

        when(bookMapper.findById(1)).thenReturn(book);
        when(holdMapper.findExistingHold(1, 1)).thenReturn(null);
        when(holdMapper.countActiveByUserId(1)).thenReturn(3L);

        assertThrows(AppException.class, () -> holdService.createHold(1, 1));
    }

    @Test
    void cancelHold_shouldCancelAndReleaseItem() {
        Hold hold = new Hold();
        hold.setId(10);
        hold.setUserId(1);
        hold.setBookId(1);
        hold.setBookItemId(100);
        hold.setStatus("ready");

        when(holdMapper.findById(10)).thenReturn(hold);

        holdService.cancelHold(10, 1);
        verify(holdMapper).updateStatus(10, "cancelled");
        verify(bookItemMapper).updateStatus(100, "available");
        verify(bookMapper).incrementAvailable(1);
    }

    @Test
    void cancelHold_shouldRejectWrongUser() {
        Hold hold = new Hold();
        hold.setId(10);
        hold.setUserId(2);

        when(holdMapper.findById(10)).thenReturn(hold);
        assertThrows(AppException.class, () -> holdService.cancelHold(10, 1));
    }

    @Test
    void getNextPendingHold_shouldReturnOldestPending() {
        Hold hold = new Hold();
        hold.setId(1);
        hold.setUserId(2);
        hold.setStatus("pending");

        when(holdMapper.findNextPendingByBookId(1)).thenReturn(hold);

        Hold result = holdService.getNextPendingHold(1);
        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void getNextPendingHold_shouldReturnNullWhenEmpty() {
        when(holdMapper.findNextPendingByBookId(1)).thenReturn(null);
        assertNull(holdService.getNextPendingHold(1));
    }
}
