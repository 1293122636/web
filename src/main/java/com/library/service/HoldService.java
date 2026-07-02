package com.library.service;

import com.library.entity.AuditLog;
import com.library.entity.*;
import com.library.exception.AppException;
import com.library.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class HoldService {

    private static final Logger log = LoggerFactory.getLogger(HoldService.class);

    private final HoldMapper holdMapper;
    private final BookMapper bookMapper;
    private final BookItemMapper bookItemMapper;
    private final BookService bookService;
    private final AuditLogMapper auditLogMapper;
    private static final int MAX_HOLDS = 3;

    public HoldService(HoldMapper holdMapper, BookMapper bookMapper,
                       BookItemMapper bookItemMapper, BookService bookService,
                       AuditLogMapper auditLogMapper) {
        this.holdMapper = holdMapper;
        this.bookMapper = bookMapper;
        this.bookItemMapper = bookItemMapper;
        this.bookService = bookService;
        this.auditLogMapper = auditLogMapper;
    }

    @Transactional
    public Hold createHold(Integer userId, Integer bookId) {
        // 1. Book must exist and have zero available copies
        Book book = bookMapper.findById(bookId);
        if (book == null) throw AppException.notFound("Book not found");
        if (book.getAvailable() > 0) {
            throw AppException.badRequest("Book has available copies — borrow directly");
        }

        // 2. No duplicate holds
        Hold existing = holdMapper.findExistingHold(userId, bookId);
        if (existing != null) throw AppException.conflict("You already have a hold on this book");

        // 3. Max holds limit
        long activeCount = holdMapper.countActiveByUserId(userId);
        if (activeCount >= MAX_HOLDS) {
            throw AppException.badRequest("Max " + MAX_HOLDS + " holds allowed");
        }

        Hold hold = new Hold();
        hold.setUserId(userId);
        hold.setBookId(bookId);
        holdMapper.insert(hold);
        audit("hold:create", "hold:" + hold.getId(), "Created hold for bookId=" + bookId);
        return holdMapper.findById(hold.getId());
    }

    @Transactional
    public void cancelHold(Integer holdId, Integer userId) {
        Hold hold = holdMapper.findById(holdId);
        if (hold == null) throw AppException.notFound("Hold not found");
        if (!hold.getUserId().equals(userId)) throw AppException.forbidden("Unauthorized");
        if (List.of("fulfilled", "cancelled", "expired").contains(hold.getStatus())) {
            throw AppException.badRequest("Hold already resolved");
        }

        // Release reserved item if hold was ready
        if ("ready".equals(hold.getStatus()) && hold.getBookItemId() != null) {
            bookService.validateItemStatus("on_hold", "available");
            bookItemMapper.updateStatus(hold.getBookItemId(), "available");
            bookMapper.incrementAvailable(hold.getBookId());
        }

        holdMapper.updateStatus(holdId, "cancelled");
        audit("hold:cancel", "hold:" + holdId, "Cancelled hold");
    }

    public List<Hold> getMyHolds(Integer userId) {
        expireReadyHolds();
        return holdMapper.findByUserId(userId);
    }

    public Map<String, Object> listHolds(String status, Integer bookId, int page, int limit) {
        expireReadyHolds();
        int offset = (page - 1) * limit;
        List<Hold> holds = holdMapper.findAllPage(status, bookId, offset, limit);
        long total = holdMapper.countAllPage(status, bookId);

        Map<String, Object> result = new HashMap<>();
        result.put("holds", holds);
        result.put("total", total);
        return result;
    }

    @Transactional
    public Hold fulfillHold(Integer holdId) {
        Hold hold = holdMapper.findById(holdId);
        if (hold == null) throw AppException.notFound("Hold not found");
        if (!"ready".equals(hold.getStatus())) {
            throw AppException.badRequest("Hold must be in ready status");
        }

        bookService.validateItemStatus("on_hold", "borrowed");
        holdMapper.fulfill(holdId, "fulfilled");

        if (hold.getBookItemId() != null) {
            bookItemMapper.updateStatus(hold.getBookItemId(), "borrowed");
        }

        return holdMapper.findById(holdId);
    }

    /**
     * Get the first pending hold in queue for a book.
     */
    public Hold getNextPendingHold(Integer bookId) {
        return holdMapper.findNextPendingByBookId(bookId);
    }

    public long countPendingByBookId(Integer bookId) {
        return holdMapper.countPendingByBookId(bookId);
    }

    /**
     * Auto-expire ready holds past their pickup window (3 days).
     */
    private void expireReadyHolds() {
        List<Hold> expired = holdMapper.findExpiredReadyHolds();
        for (Hold h : expired) {
            try {
                holdMapper.updateStatus(h.getId(), "expired");
                // Release item back to available
                if (h.getBookItemId() != null) {
                    BookItem item = bookItemMapper.findById(h.getBookItemId());
                    if (item != null && "on_hold".equals(item.getStatus())) {
                        bookService.validateItemStatus("on_hold", "available");
                        bookItemMapper.updateStatus(h.getBookItemId(), "available");
                        bookMapper.incrementAvailable(h.getBookId());
                    }
                }
            } catch (Exception e) {
                log.warn("预约过期处理失败: holdId={}", h.getId(), e);
            }
        }
    }

    private void audit(String action, String target, String detail) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setTarget(target);
        auditLog.setDetail(detail);
        try {
            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            log.error("审计日志写入失败: action={}, target={}", action, target, e);
        }
    }
}
