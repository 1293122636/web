package com.library.dto.response;

import lombok.Data;

@Data
public class StatsOverviewResponse {
    private long totalBooks;
    private long totalReaders;
    private long activeBorrows;
    private long totalCategories;
    private long overdueCount;
}
