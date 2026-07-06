package com.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PageResponse<T> {
    private java.util.List<T> data;
    private long total;
    private int page;
    private int limit;
    private int pages;
}
