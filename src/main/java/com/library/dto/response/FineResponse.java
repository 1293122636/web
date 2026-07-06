package com.library.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FineResponse {
    private Integer id;
    private Double amount;
    private String type;
    private Boolean paid;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private UserRef user;
    private BorrowRef borrowRecord;
}
