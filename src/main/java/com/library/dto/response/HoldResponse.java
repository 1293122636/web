package com.library.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HoldResponse {
    private Integer id;
    private Integer userId;
    private Integer bookId;
    private Integer bookItemId;
    private String status;
    private LocalDateTime requestDate;
    private LocalDateTime expiryDate;
    private LocalDateTime fulfilledAt;
    private BookRef book;
    private UserRef user;
}
