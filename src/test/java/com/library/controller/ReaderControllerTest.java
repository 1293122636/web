package com.library.controller;

import com.library.entity.User;
import com.library.service.UserService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReaderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    private final String adminToken = "Bearer test-admin-token";

    @BeforeEach
    void setUp() {
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1);
        when(jwtUtil.getRoleFromToken(anyString())).thenReturn("admin");
    }

    @Test
    void list_shouldReturnReaders() throws Exception {
        User u = new User();
        u.setId(1);
        u.setUsername("reader1");
        u.setRole("reader");
        when(userService.findAll()).thenReturn(List.of(u));

        mockMvc.perform(get("/api/readers").header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("reader1"));
    }

    @Test
    void getById_shouldReturnReader() throws Exception {
        User u = new User();
        u.setId(1);
        u.setUsername("reader1");
        when(userService.findById(1)).thenReturn(u);

        mockMvc.perform(get("/api/readers/1").header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("reader1"));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(userService.findById(999)).thenThrow(new com.library.exception.AppException(404, "用户不存在"));

        mockMvc.perform(get("/api/readers/999").header("Authorization", adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateByAdmin_shouldUpdateReader() throws Exception {
        User u = new User();
        u.setId(1);
        u.setName("Updated");
        when(userService.findById(1)).thenReturn(u);

        mockMvc.perform(put("/api/readers/1")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated\",\"phone\":\"138\",\"email\":\"a@a.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));

        verify(userService).update(eq(1), eq("Updated"), eq("138"), eq("a@a.com"));
    }
}
