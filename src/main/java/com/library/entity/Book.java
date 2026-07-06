package com.library.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Book {
    private Integer id;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private Integer year;
    private Integer total;
    private Integer available;
    private String status;
    private String location;
    private String cover;
    private String desc;
    private String clcNumber;
    private String physicalDesc;
    private String language;
    private String country;
    private Integer categoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Transient
    private Category category;
    private Integer itemsCount;

    public void setStatusAndUpdate(String newStatus) {
        this.status = newStatus;
    }
}
