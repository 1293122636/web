package com.library.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BorrowRecord {
    private Integer id;
    private Integer userId;
    private Integer bookId;
    private Integer bookItemId;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private Boolean renewed;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Transient
    private User user;
    private Book book;
    private BookItem bookItem;
}
