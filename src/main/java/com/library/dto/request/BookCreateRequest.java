package com.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BookCreateRequest {
    @NotBlank
    private String isbn;
    @NotBlank
    private String title;
    @NotBlank
    private String author;
    private String publisher;
    private Integer year;
    private Integer total;
    private String location;
    private String desc;
    private String clcNumber;
    private String physicalDesc;
    private String cover;
    private String language;
    private String country;
    private Integer categoryId;
}
