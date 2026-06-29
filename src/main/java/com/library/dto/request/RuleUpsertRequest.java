package com.library.dto.request;

import lombok.Data;

@Data
public class RuleUpsertRequest {
    private Integer patronCategoryId;
    private Integer itemTypeId;
    private Integer maxBorrows;
    private Integer loanDays;
    private Integer renewals;
    private Integer renewalDays;
    private java.math.BigDecimal finePerDay;
}
