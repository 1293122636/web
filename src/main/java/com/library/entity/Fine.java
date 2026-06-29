package com.library.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Fine {
    private Integer id;
    private BigDecimal amount;
    private Boolean paid;
    private LocalDateTime paidAt;
    private Integer borrowRecordId;
    private Integer userId;
    private String type;
    private LocalDateTime createdAt;

    // Transient
    private User user;
    private BorrowRecord borrowRecord;
}
