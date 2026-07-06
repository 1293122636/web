package com.library.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AuditLog {
    private Integer id;
    private Integer userId;
    private String action;
    private String target;
    private String detail;
    private LocalDateTime createdAt;
}
