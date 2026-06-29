package com.library.dto.request;

import lombok.Data;

@Data
public class ReaderUpdateRequest {
    private String name;
    private String phone;
    private String email;
}
