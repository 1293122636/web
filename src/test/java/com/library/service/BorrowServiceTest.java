package com.library.service;

import com.library.dto.request.BorrowRequest;
import com.library.entity.*;
import com.library.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BorrowServiceTest extends AbstractServiceTest {

    private BorrowService borrowService;
    private FineService fineService;
    private HoldService holdService;
    private RuleService ruleService;
    private BookService bookService;

    @BeforeEach
    void setUp() {
        ruleService = new RuleService(circulationRuleMapper);
        fineService = new FineService(fineMapper, userMapper);
        bookService = new BookService(bookMapper, bookItemMapper, categoryMapper);
        holdService = new HoldService(holdMapper, bookMapper, bookItemMapper, bookService);
        borrowService = new BorrowService(
                borrowRecordMapper, bookMapper, bookItemMapper, userMapper,
                fineMapper, auditLogMapper, ruleService, fineService,
                holdService, bookService, holdMapper);
    }

    // ===== Borrow =====

    @Test
    void borrow_shouldCreateBorrowRecord() {
        BorrowRequest req = new BorrowRequest();
        req.setBookId(1);

        User user = new User();
        user.setId(1);

        Book book = new Book();
        book.setId(1);
        book.setTitle("Test Book");
        book.setAvailable(1);
        book.setTotal(2);

        BookItem item = new BookItem();
        item.setId(10);
        item.setStatus("available");
        item.setBookId(1);
        item.setItemTypeId(1);

        ItemType itemType = new ItemType();
        itemType.setId(1);
        itemType.setLoanDays(30);

        CirculationRule rule = new CirculationRule();
        rule.setMaxBorrows(5);
        rule.setLoanDays(30);
        rule.setRenewals(1);
        rule.setRenewalDays(15);
        rule.setFinePerDay(BigDecimal.valueOf(0.10));

        when(userMapper.findById(1)).thenReturn(user);
        when(bookItemMapper.findFirstAvailableByBookId(1)).thenReturn(item);
        when(bookItemMapper.findById(10)).thenReturn(item);
        when(bookMapper.findById(1)).thenReturn(book);
        when(circulationRuleMapper.findDefault()).thenReturn(rule);
        when(borrowRecordMapper.findActiveByUserAndBook(1, 1)).thenReturn(null);
        when(borrowRecordMapper.countActiveByUserId(1)).thenReturn(0L);
        when(bookMapper.decrementAvailable(1)).thenReturn(1);
        when(bookItemMapper.updateStatus(10, "borrowed")).thenReturn(1);

        doAnswer(inv -> {
            BorrowRecord r = inv.getArgument(0);
            r.setId(100);
            return null;
        }).when(borrowRecordMapper).insert(any(BorrowRecord.class));

        BorrowRecord saved = new BorrowRecord();
        saved.setId(100);
        saved.setUserId(1);
        saved.setBookId(1);
        saved.setStatus("active");
        when(borrowRecordMapper.findById(100)).thenReturn(saved);

        BorrowRecord result = borrowService.borrow(1, req);
        assertNotNull(result);
        assertEquals(100, result.getId());
        verify(borrowRecordMapper).insert(any(BorrowRecord.class));
        verify(bookMapper).decrementAvailable(1);
    }

    @Test
    void borrow_shouldThrowWhenAlreadyBorrowed() {
        BorrowRequest req = new BorrowRequest();
        req.setBookId(1);

        Book book = new Book();
        book.setId(1);
        book.setAvailable(1);

        BookItem item = new BookItem();
        item.setId(10);
        item.setStatus("available");
        item.setBookId(1);
        item.setItemTypeId(1);

        User user = new User();
        user.setId(1);

        when(bookMapper.findById(1)).thenReturn(book);
        when(bookItemMapper.findFirstAvailableByBookId(1)).thenReturn(item);
        when(bookItemMapper.findById(10)).thenReturn(item);
        when(userMapper.findById(1)).thenReturn(user);
        when(borrowRecordMapper.findActiveByUserAndBook(1, 1)).thenReturn(new BorrowRecord());

        assertThrows(AppException.class, () -> borrowService.borrow(1, req));
    }

    @Test
    void borrow_shouldThrowWhenExceedingLimit() {
        BorrowRequest req = new BorrowRequest();
        req.setBookId(1);

        Book book = new Book();
        book.setId(1);
        book.setAvailable(1);

        BookItem item = new BookItem();
        item.setId(10);
        item.setStatus("available");
        item.setBookId(1);
        item.setItemTypeId(1);

        User user = new User();
        user.setId(1);

        CirculationRule rule = new CirculationRule();
        rule.setMaxBorrows(2);
        rule.setLoanDays(30);
        rule.setRenewals(1);
        rule.setRenewalDays(15);
        rule.setFinePerDay(BigDecimal.valueOf(0.10));

        when(bookMapper.findById(1)).thenReturn(book);
        when(bookItemMapper.findFirstAvailableByBookId(1)).thenReturn(item);
        when(bookItemMapper.findById(10)).thenReturn(item);
        when(userMapper.findById(1)).thenReturn(user);
        when(borrowRecordMapper.findActiveByUserAndBook(1, 1)).thenReturn(null);
        when(borrowRecordMapper.countActiveByUserId(1)).thenReturn(5L);
        when(circulationRuleMapper.findDefault()).thenReturn(rule);

        assertThrows(AppException.class, () -> borrowService.borrow(1, req));
    }

    @Test
    void borrow_shouldThrowWhenBookItemNotAvailable() {
        BorrowRequest req = new BorrowRequest();
        req.setBookItemId(10);

        BookItem item = new BookItem();
        item.setStatus("borrowed");

        when(bookItemMapper.findById(10)).thenReturn(item);

        assertThrows(AppException.class, () -> borrowService.borrow(1, req));
    }

    @Test
    void borrow_shouldRequireBookIdOrItemId() {
        BorrowRequest req = new BorrowRequest();
        assertThrows(AppException.class, () -> borrowService.borrow(1, req));
    }

    // ===== Return =====

    @Test
    void returnBook_shouldCompleteReturn() {
        BorrowRecord record = new BorrowRecord();
        record.setId(50);
        record.setUserId(1);
        record.setBookId(1);
        record.setBookItemId(10);
        record.setStatus("active");
        record.setDueDate(LocalDateTime.now().plusDays(5));
        record.setBorrowDate(LocalDateTime.now().minusDays(10));

        User user = new User();
        user.setId(1);
        record.setUser(user);

        BookItem bookItem = new BookItem();
        bookItem.setId(10);
        bookItem.setItemTypeId(1);
        record.setBookItem(bookItem);

        BorrowRecord returned = new BorrowRecord();
        returned.setId(50);
        returned.setStatus("returned");

        when(borrowRecordMapper.findById(50)).thenReturn(record, returned);

        CirculationRule rule = new CirculationRule();
        rule.setFinePerDay(BigDecimal.valueOf(0.10));
        when(circulationRuleMapper.findDefault()).thenReturn(rule);

        when(holdMapper.findNextPendingByBookId(1)).thenReturn(null);
        when(bookMapper.incrementAvailable(1)).thenReturn(1);

        BorrowRecord result = borrowService.returnBook(50, 1, false);
        assertNotNull(result);
        assertEquals("returned", result.getStatus());
        verify(borrowRecordMapper).returnBook(eq(50), any(), eq("returned"));
        verify(bookMapper).incrementAvailable(1);
    }

    @Test
    void returnBook_shouldThrowForWrongUser() {
        BorrowRecord record = new BorrowRecord();
        record.setId(50);
        record.setUserId(2);
        record.setStatus("active");

        when(borrowRecordMapper.findById(50)).thenReturn(record);

        assertThrows(AppException.class, () -> borrowService.returnBook(50, 1, false));
    }

    @Test
    void returnBook_shouldThrowWhenAlreadyReturned() {
        BorrowRecord record = new BorrowRecord();
        record.setId(50);
        record.setStatus("returned");

        when(borrowRecordMapper.findById(50)).thenReturn(record);

        assertThrows(AppException.class, () -> borrowService.returnBook(50, 1, false));
    }

    @Test
    void returnBook_shouldAllowAdminForAnyUser() {
        BorrowRecord record = new BorrowRecord();
        record.setId(50);
        record.setUserId(2);
        record.setBookId(1);
        record.setBookItemId(10);
        record.setStatus("active");
        record.setDueDate(LocalDateTime.now().plusDays(5));

        User user = new User();
        user.setId(2);
        record.setUser(user);

        BookItem bookItem = new BookItem();
        bookItem.setId(10);
        bookItem.setItemTypeId(1);
        record.setBookItem(bookItem);

        BorrowRecord returned = new BorrowRecord();
        returned.setId(50);
        returned.setStatus("returned");

        when(borrowRecordMapper.findById(50)).thenReturn(record, returned);

        CirculationRule rule = new CirculationRule();
        rule.setFinePerDay(BigDecimal.valueOf(0.10));
        when(circulationRuleMapper.findDefault()).thenReturn(rule);
        when(holdMapper.findNextPendingByBookId(1)).thenReturn(null);
        when(bookMapper.incrementAvailable(1)).thenReturn(1);

        assertDoesNotThrow(() -> borrowService.returnBook(50, 1, true));
    }

    // ===== Renew =====

    @Test
    void renew_shouldExtendDueDate() {
        BorrowRecord record = new BorrowRecord();
        record.setId(50);
        record.setUserId(1);
        record.setStatus("active");
        record.setRenewed(false);
        record.setDueDate(LocalDateTime.now());

        BookItem bookItem = new BookItem();
        bookItem.setItemTypeId(1);
        record.setBookItem(bookItem);

        User user = new User();
        record.setUser(user);

        CirculationRule rule = new CirculationRule();
        rule.setRenewalDays(15);
        rule.setRenewals(1);

        when(borrowRecordMapper.findById(50)).thenReturn(record);
        when(circulationRuleMapper.findDefault()).thenReturn(rule);

        var result = borrowService.renew(50, 1);
        assertEquals(50, result.get("id"));
        assertTrue((Boolean) result.get("renewed"));
        assertEquals(15, result.get("renewedDays"));
        verify(borrowRecordMapper).renew(eq(50), any());
    }

    @Test
    void renew_shouldThrowWhenAlreadyRenewed() {
        BorrowRecord record = new BorrowRecord();
        record.setId(50);
        record.setUserId(1);
        record.setStatus("active");
        record.setRenewed(true);

        BookItem bookItem = new BookItem();
        bookItem.setItemTypeId(1);
        record.setBookItem(bookItem);

        User user = new User();
        record.setUser(user);

        CirculationRule rule = new CirculationRule();
        rule.setRenewals(1);

        when(borrowRecordMapper.findById(50)).thenReturn(record);
        when(circulationRuleMapper.findDefault()).thenReturn(rule);

        assertThrows(AppException.class, () -> borrowService.renew(50, 1));
    }

    @Test
    void renew_shouldThrowForWrongUser() {
        BorrowRecord record = new BorrowRecord();
        record.setId(50);
        record.setUserId(2);
        record.setStatus("active");

        when(borrowRecordMapper.findById(50)).thenReturn(record);

        assertThrows(AppException.class, () -> borrowService.renew(50, 1));
    }

    @Test
    void renew_shouldThrowWhenNotActive() {
        BorrowRecord record = new BorrowRecord();
        record.setId(50);
        record.setUserId(1);
        record.setStatus("returned");

        when(borrowRecordMapper.findById(50)).thenReturn(record);

        assertThrows(AppException.class, () -> borrowService.renew(50, 1));
    }
}
