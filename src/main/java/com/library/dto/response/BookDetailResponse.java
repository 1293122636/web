package com.library.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookDetailResponse {
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
    private CategoryResponse category;
    private Integer itemsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<BookItemResponse> items;
}
