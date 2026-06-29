package com.library.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BorrowRecordResponse {
    private Integer id;
    private Integer userId;
    private Integer bookId;
    private Integer bookItemId;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private String status;
    private Boolean renewed;
    private BookRef book;
    private BookItemRef bookItem;
    private UserRef user;
    private List<FineRef> fines;
}
