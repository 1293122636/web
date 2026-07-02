package com.library.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RuleUpsertRequest {
    @NotNull(message = "读者分类不能为空")
    private Integer patronCategoryId;

    @NotNull(message = "文献类型不能为空")
    private Integer itemTypeId;

    @NotNull(message = "最大借阅数不能为空")
    @Min(value = 1, message = "最大借阅数至少为1")
    private Integer maxBorrows;

    @NotNull(message = "借阅天数不能为空")
    @Min(value = 1, message = "借阅天数至少为1")
    private Integer loanDays;

    @NotNull(message = "续借次数不能为空")
    @Min(value = 0, message = "续借次数不能为负")
    private Integer renewals;

    @NotNull(message = "续借天数不能为空")
    @Min(value = 1, message = "续借天数至少为1")
    private Integer renewalDays;

    @NotNull(message = "逾期日罚金不能为空")
    @DecimalMin(value = "0.00", message = "逾期日罚金不能为负")
    private BigDecimal finePerDay;
}
