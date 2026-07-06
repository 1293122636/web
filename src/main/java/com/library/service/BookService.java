package com.library.service;

import com.library.dto.response.BookDetailResponse;
import com.library.dto.response.BookItemResponse;
import com.library.dto.response.CategoryResponse;
import com.library.entity.AuditLog;
import com.library.entity.Book;
import com.library.entity.BookItem;
import com.library.entity.BorrowRecord;
import com.library.exception.AppException;
import com.library.mapper.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    private final BookMapper bookMapper;
    private final BookItemMapper bookItemMapper;
    private final CategoryMapper categoryMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final AuditLogMapper auditLogMapper;
    
    // Status transition validation
    private static final Map<String, List<String>> STATUS_TRANSITIONS = new HashMap<>();
    static {
        STATUS_TRANSITIONS.put("available", Arrays.asList("borrowed", "on_hold", "repairing", "lost", "withdrawn"));
        STATUS_TRANSITIONS.put("borrowed", Arrays.asList("available", "lost", "on_hold"));
        STATUS_TRANSITIONS.put("on_hold", Arrays.asList("available", "borrowed", "expired"));
        STATUS_TRANSITIONS.put("repairing", Arrays.asList("available", "lost", "withdrawn"));
        STATUS_TRANSITIONS.put("lost", Arrays.asList("available", "withdrawn"));
        STATUS_TRANSITIONS.put("withdrawn", Collections.emptyList());
    }

    public BookService(BookMapper bookMapper, BookItemMapper bookItemMapper, CategoryMapper categoryMapper, BorrowRecordMapper borrowRecordMapper, AuditLogMapper auditLogMapper) {
        this.bookMapper = bookMapper;
        this.bookItemMapper = bookItemMapper;
        this.categoryMapper = categoryMapper;
        this.borrowRecordMapper = borrowRecordMapper;
        this.auditLogMapper = auditLogMapper;
    }
    
    public Map<String, Object> list(Map<String, Object> params) {
        int page = Math.max(1, params.get("page") != null ? (Integer) params.get("page") : 1);
        int limit = Math.min(50, Math.max(1, params.get("limit") != null ? (Integer) params.get("limit") : 20));
        params.put("offset", (page - 1) * limit);

        List<Book> books = bookMapper.searchBooks(params);
        long total = bookMapper.countBooks(params);
        int pages = (int) Math.ceil((double) total / limit);

        Map<String, Object> result = new HashMap<>();
        result.put("books", books);
        result.put("total", total);
        result.put("page", page);
        result.put("limit", limit);
        result.put("pages", pages);
        return result;
    }

    public BookDetailResponse getById(Integer id) {
        Book book = bookMapper.findById(id);
        if (book == null) throw AppException.notFound("Book not found");

        BookDetailResponse resp = new BookDetailResponse();
        resp.setId(book.getId());
        resp.setIsbn(book.getIsbn());
        resp.setTitle(book.getTitle());
        resp.setAuthor(book.getAuthor());
        resp.setPublisher(book.getPublisher());
        resp.setYear(book.getYear());
        resp.setTotal(book.getTotal());
        resp.setAvailable(book.getAvailable());
        resp.setStatus(book.getStatus());
        resp.setLocation(book.getLocation());
        resp.setCover(book.getCover());
        resp.setDesc(book.getDesc());
        resp.setClcNumber(book.getClcNumber());
        resp.setPhysicalDesc(book.getPhysicalDesc());
        resp.setLanguage(book.getLanguage());
        resp.setCountry(book.getCountry());
        resp.setCategoryId(book.getCategoryId());
        resp.setCreatedAt(book.getCreatedAt());
        resp.setUpdatedAt(book.getUpdatedAt());

        if (book.getCategory() != null) {
            int bookCount = (int) categoryMapper.countBooksByCategory(book.getCategory().getId());
            resp.setCategory(new CategoryResponse(book.getCategory().getId(), book.getCategory().getName(),
                    book.getCategory().getDesc(), bookCount));
        }

        List<BookItem> items = bookItemMapper.findByBookId(id);
        List<BookItemResponse> itemResponses = items.stream().map(item -> {
            BookItemResponse ir = new BookItemResponse();
            ir.setId(item.getId());
            ir.setBarcode(item.getBarcode());
            ir.setCallNumber(item.getCallNumber());
            ir.setLocation(item.getLocation());
            ir.setCampus(item.getCampus());
            ir.setCondition(item.getCondition());
            ir.setStatus(item.getStatus());
            ir.setPrice(item.getPrice() != null ? item.getPrice().doubleValue() : null);
            ir.setAcquiredAt(item.getAcquiredAt());
            ir.setRequests(item.getRequests());
            ir.setBookId(item.getBookId());
            ir.setItemTypeId(item.getItemTypeId());
            ir.setCreatedAt(item.getCreatedAt());
            ir.setUpdatedAt(item.getUpdatedAt());
            return ir;
        }).collect(Collectors.toList());
        resp.setItems(itemResponses);
        resp.setItemsCount((int) bookItemMapper.countByBookId(id));

        return resp;
    }

    public List<BookItemResponse> getItemsByBookId(Integer id) {
        // Verify book exists
        Book book = bookMapper.findById(id);
        if (book == null) throw AppException.notFound("Book not found");

        List<BookItem> items = bookItemMapper.findByBookId(id);
        return items.stream().map(item -> {
            BookItemResponse ir = new BookItemResponse();
            ir.setId(item.getId());
            ir.setBarcode(item.getBarcode());
            ir.setCallNumber(item.getCallNumber());
            ir.setLocation(item.getLocation());
            ir.setCampus(item.getCampus());
            ir.setCondition(item.getCondition());
            ir.setStatus(item.getStatus());
            ir.setPrice(item.getPrice() != null ? item.getPrice().doubleValue() : null);
            ir.setAcquiredAt(item.getAcquiredAt());
            ir.setRequests(item.getRequests());
            ir.setBookId(item.getBookId());
            ir.setItemTypeId(item.getItemTypeId());
            ir.setCreatedAt(item.getCreatedAt());
            ir.setUpdatedAt(item.getUpdatedAt());
            return ir;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Book create(com.library.dto.request.BookCreateRequest data) {
        Book book = new Book();
        book.setIsbn(data.getIsbn());
        book.setTitle(data.getTitle());
        book.setAuthor(data.getAuthor());
        book.setPublisher(data.getPublisher());
        book.setYear(data.getYear());
        int total = data.getTotal() != null ? data.getTotal() : 1;
        book.setTotal(total);
        book.setAvailable(total);
        book.setStatus("available");
        book.setLocation(data.getLocation());
        book.setCover(data.getCover());
        book.setDesc(data.getDesc());
        book.setClcNumber(data.getClcNumber());
        book.setPhysicalDesc(data.getPhysicalDesc());
        book.setLanguage(data.getLanguage());
        book.setCountry(data.getCountry());
        book.setCategoryId(data.getCategoryId());
        bookMapper.insert(book);
        audit("create", "book:" + book.getId(), "Created book: " + book.getTitle());
        return book;
    }

    @Transactional
    public Book update(Integer id, Map<String, Object> data) {
        Book current = bookMapper.findById(id);
        if (current == null) throw AppException.notFound("Book not found");

        if (data.containsKey("total") && data.get("total") != null) {
            int newTotal = (Integer) data.get("total");
            int borrowed = current.getTotal() - current.getAvailable();
            if (newTotal < borrowed) {
                throw AppException.badRequest("Cannot reduce total below " + borrowed);
            }
            int diff = newTotal - current.getTotal();
            int newAvailable = current.getAvailable() + diff;
            bookMapper.updateTotalAndAvailable(id, newTotal, newAvailable);
            data.remove("total");
        }


        if (!data.isEmpty()) {
            Book updateBook = new Book();
            updateBook.setId(id);
            if (data.containsKey("title")) updateBook.setTitle((String) data.get("title"));
            if (data.containsKey("author")) updateBook.setAuthor((String) data.get("author"));
            if (data.containsKey("isbn")) updateBook.setIsbn((String) data.get("isbn"));
            if (data.containsKey("publisher")) updateBook.setPublisher((String) data.get("publisher"));
            if (data.containsKey("location")) updateBook.setLocation((String) data.get("location"));
            if (data.containsKey("cover")) {
                String newCover = (String) data.get("cover");
                updateBook.setCover(newCover);
                removeOldCoverIfChanged(current.getCover(), newCover);
            }
            if (data.containsKey("desc")) updateBook.setDesc((String) data.get("desc"));
            if (data.containsKey("clcNumber")) updateBook.setClcNumber((String) data.get("clcNumber"));
            if (data.containsKey("physicalDesc")) updateBook.setPhysicalDesc((String) data.get("physicalDesc"));
            if (data.containsKey("language")) updateBook.setLanguage((String) data.get("language"));
            if (data.containsKey("country")) updateBook.setCountry((String) data.get("country"));
            if (data.containsKey("categoryId")) updateBook.setCategoryId((Integer) data.get("categoryId"));
            
            bookMapper.update(updateBook);
        }

        Book updated = bookMapper.findById(id);
        audit("update", "book:" + id, "Updated book: " + (updated != null ? updated.getTitle() : id));
        return updated;
    }

    @Transactional
    public void remove(Integer id) {
        long count = bookItemMapper.countByBookId(id);
        if (count > 0) throw AppException.badRequest("Cannot delete book with " + count + " existing copies");
        bookMapper.deleteById(id);
        audit("delete", "book:" + id, "Deleted book id: " + id);
    }

    public Map<String, Object> getFacets(Map<String, Object> params) {
        Map<String, Object> facets = new HashMap<>();

        // campus facet — from book_items JOIN books
        facets.put("campus", bookItemMapper.countByCampus(params));

        // location facet — from book_items JOIN books
        facets.put("location", bookItemMapper.countByLocation(params));

        // language facet — from books
        facets.put("language", bookMapper.countByLanguage(params));

        // subject (category) facet — from books JOIN categories
        facets.put("subject", bookMapper.countByCategory(params));

        // yearRange facet — decade grouping
        facets.put("yearRange", bookMapper.countByYearDecade(params));

        return Map.of("facets", facets);
    }

    public void validateItemStatus(String current, String next) {
        List<String> allowed = STATUS_TRANSITIONS.get(current);
        if (allowed == null || !allowed.contains(next)) {
            throw AppException.badRequest("Invalid status transition: " + current + " → " + next);
        }
    }

    public Map<String, Object> lookupByBarcode(String barcode) {
        BookItem item = bookItemMapper.findByBarcode(barcode);
        if (item == null) return null;
        Map<String, Object> result = new HashMap<>();
        result.put("item", item);
        // Load active borrow for circulation display
        BorrowRecord activeBorrow = borrowRecordMapper.findActiveByBookItemId(item.getId());
        if (activeBorrow != null) {
            result.put("currentBorrow", activeBorrow);
        }
        return result;
    }

     private void removeOldCoverIfChanged(String oldCover,String newCover){
        if(oldCover!=null&&oldCover.startsWith("/covers/")&&!oldCover.equals(newCover)){
            try{
                Path oldFile =Paths.get("src/main/resources/static"+oldCover);
                Files.deleteIfExists(oldFile);
                log.info("Deleted old cover:{}",oldCover);
            }catch(IOException e){
                log.warn("Failed to delete old cover:{}",oldCover,e);
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
