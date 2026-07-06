package com.library.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookItem {
    private Integer id;
    private String barcode;
    private String callNumber;
    private String location;
    private String condition;
    private String status;
    private java.math.BigDecimal price;
    private LocalDateTime acquiredAt;
    private String notes;
    private String campus;
    private Integer requests;
    private Integer bookId;
    private Integer itemTypeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Transient
    private ItemType itemType;
    private Book book;
}
