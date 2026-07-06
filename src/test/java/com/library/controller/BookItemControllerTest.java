package com.library.controller;

import com.library.service.BookService;
import com.library.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

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
    void lookupByBarcode_shouldReturnItem() throws Exception {
        when(bookService.lookupByBarcode("BAR001")).thenReturn(Map.of("item", Map.of("barcode", "BAR001", "status", "available")));

        mockMvc.perform(get("/api/book-items/BAR001").header("Authorization", readerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.barcode").value("BAR001"));
    }

    @Test
    void lookupByBarcode_shouldReturn404WhenNotFound() throws Exception {
        when(bookService.lookupByBarcode("INVALID")).thenReturn(null);

        mockMvc.perform(get("/api/book-items/INVALID").header("Authorization", readerToken))
                .andExpect(status().isNotFound());
    }
}
