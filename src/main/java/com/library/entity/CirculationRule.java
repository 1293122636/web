package com.library.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CirculationRule {
    private Integer id;
    private Integer patronCategoryId;
    private Integer itemTypeId;
    private Integer maxBorrows;
    private Integer loanDays;
    private Integer renewals;
    private Integer renewalDays;
    private java.math.BigDecimal finePerDay;
    private LocalDateTime createdAt;

    // Transient
    private PatronCategory patronCategory;
    private ItemType itemType;
}
