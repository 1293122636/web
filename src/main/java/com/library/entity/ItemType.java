package com.library.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ItemType {
    private Integer id;
    private String name;
    private Integer loanDays;
    private java.math.BigDecimal fineRate;
    private LocalDateTime createdAt;
}
