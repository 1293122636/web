package com.library.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookItemResponse {
    private Integer id;
    private String barcode;
    private String callNumber;
    private String location;
    private String campus;
    private String condition;
    private String status;
    private Double price;
    private LocalDateTime acquiredAt;
    private Integer requests;
    private Integer bookId;
    private Integer itemTypeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
