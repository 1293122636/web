package com.library.controller;

import com.library.dto.request.BorrowRequest;
import com.library.entity.BorrowRecord;
import com.library.service.BorrowService;
import com.library.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BorrowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BorrowService borrowService;

    @MockBean
    private JwtUtil jwtUtil;

    private final String readerToken = "Bearer test-reader-token";

    @BeforeEach
    void setUp() {
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1);
        when(jwtUtil.getRoleFromToken(anyString())).thenReturn("reader");
    }

    @Test
    void borrow_shouldReturnBorrowRecord() throws Exception {
        BorrowRecord record = new BorrowRecord();
        record.setId(100);
        record.setUserId(1);
        record.setStatus("active");
        record.setBorrowDate(LocalDateTime.now());

        when(borrowService.borrow(anyInt(), any(BorrowRequest.class))).thenReturn(record);

        String body = "{\"bookId\":1}";
        mockMvc.perform(post("/api/borrows/borrow")
                        .header("Authorization", readerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100));
    }
}
