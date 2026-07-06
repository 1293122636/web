package com.library.dto.response;

import lombok.Data;

@Data
public class MonthlyStatsDTO {
    private String month;
    private Long count;
}
