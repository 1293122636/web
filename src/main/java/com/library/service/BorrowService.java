package com.library.service;

import com.library.dto.request.BorrowRequest;
import com.library.entity.*;
import com.library.exception.AppException;
import com.library.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class BorrowService {

    private static final Logger logger = LoggerFactory.getLogger(BorrowService.class);

    private final BorrowRecordMapper borrowRecordMapper;
    private final BookMapper bookMapper;
    private final BookItemMapper bookItemMapper;
    private final UserMapper userMapper;
    private final AuditLogMapper auditLogMapper;
    private final RuleService ruleService;
    private final FineService fineService;
    private final HoldService holdService;
    private final BookService bookService;
    private final HoldMapper holdMapper;

    public BorrowService(BorrowRecordMapper borrowRecordMapper, BookMapper bookMapper,
                          BookItemMapper bookItemMapper, UserMapper userMapper,
                          AuditLogMapper auditLogMapper,
                          RuleService ruleService, FineService fineService,
                          HoldService holdService, BookService bookService, HoldMapper holdMapper) {
        this.borrowRecordMapper = borrowRecordMapper;
        this.bookMapper = bookMapper;
        this.bookItemMapper = bookItemMapper;
        this.userMapper = userMapper;
        this.auditLogMapper = auditLogMapper;
        this.ruleService = ruleService;
        this.fineService = fineService;
        this.holdService = holdService;
        this.bookService = bookService;
        this.holdMapper = holdMapper;
    }

    public Map<String, Object> getMyBorrows(Integer userId, int page, int limit) {
        int offset = (page - 1) * limit;
        List<BorrowRecord> records = borrowRecordMapper.findByUserIdPage(userId, offset, limit);
        long total = borrowRecordMapper.countByUserId(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("borrows", records);
        result.put("total", total);
        return result;
    }

    public Map<String, Object> listBorrows(int page, int limit) {
        int offset = (page - 1) * limit;
        List<BorrowRecord> records = borrowRecordMapper.findAllPage(offset, limit);
        long total = borrowRecordMapper.countAll();

        Map<String, Object> result = new HashMap<>();
        result.put("borrows", records);
        result.put("total", total);
        return result;
    }

    /**
     * Borrow a book — uses synchronized transactional approach.
     * Equivalent to the Prisma interactive $transaction in the original TS.
     */
    @Transactional
    public BorrowRecord borrow(Integer userId, BorrowRequest params) {
        Integer bookId = params.getBookId();
        Integer bookItemId = params.getBookItemId();

        if (bookId == null && bookItemId == null) {
            throw AppException.badRequest("bookId or bookItemId required");
        }

        Integer targetBookId = bookId;
        Integer targetItemId = bookItemId;

        // Find the item if only bookId was given
        if (targetItemId != null) {
            BookItem item = bookItemMapper.findById(targetItemId);
            if (item == null) throw AppException.notFound("Book item not found");
            if (!"available".equals(item.getStatus())) {
                throw AppException.badRequest("This copy is not available");
            }
            targetBookId = item.getBookId();
        } else {
            Book book = bookMapper.findById(targetBookId);
            if (book == null) throw AppException.notFound("Book not found");
            BookItem firstItem = bookItemMapper.findFirstAvailableByBookId(targetBookId);
            if (firstItem != null) targetItemId = firstItem.getId();
        }

        if (targetBookId == null) throw AppException.notFound("Book not found");

        User user = userMapper.findById(userId);
        BookItem item = targetItemId != null ? bookItemMapper.findById(targetItemId) : null;
        Integer patronCatId = user != null ? user.getPatronCategoryId() : null;
        Integer itemTypeId = item != null ? item.getItemTypeId() : null;

        CirculationRule rule = ruleService.getRule(patronCatId, itemTypeId);
        LocalDateTime dueDate = LocalDateTime.now().plusDays(rule.getLoanDays());

        // Transaction-level checks: re-verify availability atomically
        BorrowRecord existing = borrowRecordMapper.findActiveByUserAndBook(userId, targetBookId);
        if (existing != null) {
            throw AppException.badRequest("You already borrowed this book");
        }

        long currentCount = borrowRecordMapper.countActiveByUserId(userId);
        if (currentCount >= rule.getMaxBorrows()) {
            throw AppException.badRequest("Borrow limit exceeded: max " + rule.getMaxBorrows());
        }

        // Re-verify item is still available
        if (targetItemId != null) {
            BookItem itemCheck = bookItemMapper.findById(targetItemId);
            if (itemCheck == null || !"available".equals(itemCheck.getStatus())) {
                throw AppException.badRequest("Copy no longer available");
            }
            bookService.validateItemStatus("available", "borrowed");
        }

        // All checks passed — create borrow record
        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(targetBookId);
        record.setBookItemId(targetItemId);
        record.setBorrowDate(LocalDateTime.now());
        record.setDueDate(dueDate);
        record.setStatus("active");
        record.setRenewed(false);
        borrowRecordMapper.insert(record);

        // Update book inventory
        int updated = bookMapper.decrementAvailable(targetBookId);
        if (updated == 0) throw AppException.badRequest("该书已无可用副本");
        if (targetBookId != null) {
            bookMapper.updateStatus(targetBookId, "borrowed");
        }

        if (targetItemId != null) {
            bookItemMapper.updateStatus(targetItemId, "borrowed");
        }

        audit(userId, "borrow", "book:" + targetBookId, targetItemId != null ? "item:" + targetItemId : null);
        return borrowRecordMapper.findById(record.getId());
    }

    @Transactional
    public BorrowRecord returnBook(Integer borrowRecordId, Integer userId, boolean isAdmin) {
        BorrowRecord record = borrowRecordMapper.findById(borrowRecordId);
        if (record == null) throw AppException.notFound("Borrow record not found");
        if (!"active".equals(record.getStatus())) {
            throw AppException.badRequest("Already returned");
        }
        if (!record.getUserId().equals(userId) && !isAdmin) {
            throw AppException.forbidden("Unauthorized");
        }

        LocalDateTime now = LocalDateTime.now();
        boolean isOverdue = now.isAfter(record.getDueDate());

        CirculationRule rule = ruleService.getRule(
                record.getUser() != null ? record.getUser().getPatronCategoryId() : null,
                record.getBookItem() != null ? record.getBookItem().getItemTypeId() : null
        );

        if (isOverdue) {
            BigDecimal fineAmount = fineService.calcOverdueFine(record.getDueDate(), now, rule.getFinePerDay());
            if (fineAmount.compareTo(BigDecimal.ZERO) > 0) {
                fineService.createFine(borrowRecordId, record.getUserId(), fineAmount, "overdue");
            }
        }

        // Check for next hold in queue
        Hold nextHold = holdService.getNextPendingHold(record.getBookId());

        // Update borrow record
        borrowRecordMapper.returnBook(borrowRecordId, now, isOverdue ? "overdue" : "returned");

        if (nextHold != null && record.getBookItemId() != null) {
            // Hold promotion: item goes to on_hold
            Hold hold = holdMapper.findById(nextHold.getId());
            if (hold != null) {
                hold.updateToReady(record.getBookItemId(), now.plusDays(3));
                holdMapper.updateToReady(hold.getId(), hold.getBookItemId(), hold.getExpiryDate());
            }
            bookItemMapper.updateStatus(record.getBookItemId(), "on_hold");
            // available stays the same
        } else {
            // Normal return: increment available
            bookMapper.incrementAvailable(record.getBookId());
            if (record.getBookItemId() != null) {
                bookItemMapper.updateStatus(record.getBookItemId(), "available");
            }
        }

        audit(userId, "return", "record:" + borrowRecordId, isOverdue ? "overdue" : null);
        return borrowRecordMapper.findById(borrowRecordId);
    }

    @Transactional
    public Map<String, Object> renew(Integer recordId, Integer userId) {
        BorrowRecord record = borrowRecordMapper.findById(recordId);
        if (record == null) throw AppException.notFound("Borrow record not found");
        if (!"active".equals(record.getStatus())) throw AppException.badRequest("Cannot renew");
        if (!record.getUserId().equals(userId)) throw AppException.forbidden("Unauthorized");

        CirculationRule rule = ruleService.getRule(
                record.getUser() != null ? record.getUser().getPatronCategoryId() : null,
                record.getBookItem() != null ? record.getBookItem().getItemTypeId() : null
        );

        if (record.getRenewed()) {
            throw AppException.badRequest("Already renewed (limit: " + rule.getRenewals() + "x)");
        }

        LocalDateTime newDue = record.getDueDate().plusDays(rule.getRenewalDays());
        borrowRecordMapper.renew(recordId, newDue);

        audit(userId, "renew", "record:" + recordId, rule.getRenewalDays() + "d");

        Map<String, Object> result = new HashMap<>();
        result.put("id", recordId);
        result.put("dueDate", newDue);
        result.put("renewed", true);
        result.put("renewedDays", rule.getRenewalDays());
        return result;
    }

    public Map<String, Object> getHistory(Integer userId, int page, int limit) {
        int offset = (page - 1) * limit;
        List<BorrowRecord> records = borrowRecordMapper.findByUserIdPage(userId, offset, limit);
        long total = borrowRecordMapper.countByUserId(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("borrows", records);
        result.put("total", total);
        return result;
    }

    private void audit(Integer userId, String action, String target, String detail) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setTarget(target);
        log.setDetail(detail);
        try {
            auditLogMapper.insert(log);
        } catch (Exception e) {
            logger.error("审计日志写入失败: action={}, target={}", action, target, e);
        }
    }
}
