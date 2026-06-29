package com.library.dto.request;

import lombok.Data;

@Data
public class BorrowRequest {
    private Integer bookId;
    private Integer bookItemId;
}
