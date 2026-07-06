package com.library.controller;

import com.library.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    private final String adminToken = "Bearer test-admin-token";
    private final String readerToken = "Bearer test-reader-token";

    @BeforeEach
    void setUp() {
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1);
    }

    @Test
    void uploadCover_byAdmin_shouldReturn200() throws Exception {
        when(jwtUtil.getRoleFromToken(anyString())).thenReturn("admin");

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "fake-image-content".getBytes());

        mockMvc.perform(multipart("/api/upload/cover")
                        .file(file)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").exists())
                .andExpect(jsonPath("$.path").value(startsWith("/covers/")));
    }

    @Test
    void uploadCover_byReader_shouldReturn403() throws Exception {
        when(jwtUtil.getRoleFromToken(anyString())).thenReturn("reader");

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "fake-image-content".getBytes());

        mockMvc.perform(multipart("/api/upload/cover")
                        .file(file)
                        .header("Authorization", readerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void uploadCover_withoutToken_shouldReturn401() throws Exception {
        when(jwtUtil.getRoleFromToken(anyString())).thenReturn(null);

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "fake-image-content".getBytes());

        mockMvc.perform(multipart("/api/upload/cover")
                        .file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void uploadCover_emptyFile_shouldReturn400() throws Exception {
        when(jwtUtil.getRoleFromToken(anyString())).thenReturn("admin");

        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]);

        mockMvc.perform(multipart("/api/upload/cover")
                        .file(emptyFile)
                        .header("Authorization", adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadCover_wrongType_shouldReturn400() throws Exception {
        when(jwtUtil.getRoleFromToken(anyString())).thenReturn("admin");

        MockMultipartFile txtFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "not an image".getBytes());

        mockMvc.perform(multipart("/api/upload/cover")
                        .file(txtFile)
                        .header("Authorization", adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadCover_tooLarge_shouldReturn400() throws Exception {
        when(jwtUtil.getRoleFromToken(anyString())).thenReturn("admin");

        // 6MB file — exceeds 5MB limit
        byte[] largeContent = new byte[6 * 1024 * 1024];
        MockMultipartFile largeFile = new MockMultipartFile(
                "file", "large.jpg", "image/jpeg", largeContent);

        mockMvc.perform(multipart("/api/upload/cover")
                        .file(largeFile)
                        .header("Authorization", adminToken))
                .andExpect(status().isBadRequest());
    }
}
