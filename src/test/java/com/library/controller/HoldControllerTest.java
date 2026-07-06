package com.library.controller;

import com.library.entity.Hold;
import com.library.service.HoldService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HoldControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HoldService holdService;

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
    void createHold_shouldReturnHold() throws Exception {
        Hold hold = new Hold();
        hold.setId(1);
        hold.setUserId(1);
        hold.setBookId(1);
        hold.setStatus("pending");

        when(holdService.createHold(anyInt(), anyInt())).thenReturn(hold);

        String body = "{\"bookId\":1}";
        mockMvc.perform(post("/api/holds")
                        .header("Authorization", readerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("pending"));
    }
}
