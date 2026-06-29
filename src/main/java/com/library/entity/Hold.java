package com.library.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Hold {
    private Integer id;
    private Integer userId;
    private Integer bookId;
    private Integer bookItemId;
    private String status;
    private LocalDateTime requestDate;
    private LocalDateTime expiryDate;
    private LocalDateTime fulfilledAt;
    private LocalDateTime createdAt;

    // Transient
    private User user;
    private Book book;
    private BookItem bookItem;

    public void updateToReady(Integer assignedBookItemId, LocalDateTime expiry) {
        this.status = "ready";
        this.bookItemId = assignedBookItemId;
        this.expiryDate = expiry;
    }
}
