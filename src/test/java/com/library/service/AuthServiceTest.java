package com.library.service;

import com.library.dto.request.RegisterRequest;
import com.library.dto.response.LoginResponse;
import com.library.entity.User;
import com.library.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceTest extends AbstractServiceTest {

    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userMapper, passwordEncoder, jwtUtil);
    }

    @Test
    void register_shouldCreateUserAndReturnToken() {
        // Arrange
        RegisterRequest req = new RegisterRequest();
        req.setUsername("testuser");
        req.setPassword("pass123");
        req.setName("Test User");

        when(userMapper.findByUsername("testuser")).thenReturn(null);
        when(jwtUtil.generateToken(anyInt(), eq("reader"))).thenReturn("test-token");

        User savedUser = new User();
        savedUser.setId(42);
        savedUser.setUsername("testuser");
        savedUser.setName("Test User");
        savedUser.setRole("reader");
        savedUser.setTotalFines(BigDecimal.ZERO);
        savedUser.setCreatedAt(LocalDateTime.now());

        // Act
        doAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(42);
            return null;
        }).when(userMapper).insert(any(User.class));

        LoginResponse response = authService.register(req);

        // Assert
        assertNotNull(response);
        assertEquals("test-token", response.getToken());
        assertEquals("testuser", response.getUser().getUsername());
        assertEquals("Test User", response.getUser().getName());
        assertEquals("reader", response.getUser().getRole());

        verify(userMapper).findByUsername("testuser");
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void register_shouldThrowWhenUsernameExists() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("existing");
        req.setPassword("pass123");
        req.setName("Existing User");

        when(userMapper.findByUsername("existing")).thenReturn(new User());

        assertThrows(AppException.class, () -> authService.register(req));
    }

    @Test
    void login_shouldReturnTokenForValidCredentials() {
        User user = new User();
        user.setId(1);
        user.setUsername("admin");
        user.setPassword(passwordEncoder.encode("admin123"));
        user.setName("Admin");
        user.setRole("admin");
        user.setCreatedAt(LocalDateTime.now());

        when(userMapper.findByUsername("admin")).thenReturn(user);
        when(jwtUtil.generateToken(1, "admin")).thenReturn("admin-token");

        LoginResponse response = authService.login("admin", "admin123");

        assertNotNull(response);
        assertEquals("admin-token", response.getToken());
        assertEquals("admin", response.getUser().getUsername());
    }

    @Test
    void login_shouldThrowForInvalidPassword() {
        User user = new User();
        user.setPassword(passwordEncoder.encode("rightpass"));

        when(userMapper.findByUsername("user")).thenReturn(user);

        assertThrows(AppException.class, () -> authService.login("user", "wrongpass"));
    }

    @Test
    void login_shouldThrowForNonexistentUser() {
        when(userMapper.findByUsername("nobody")).thenReturn(null);
        assertThrows(AppException.class, () -> authService.login("nobody", "pass"));
    }

    @Test
    void getMe_shouldReturnProfile() {
        User user = new User();
        user.setId(1);
        user.setUsername("reader1");
        user.setName("Reader One");
        user.setRole("reader");
        user.setCreatedAt(LocalDateTime.now());

        when(userMapper.findById(1)).thenReturn(user);

        var profile = authService.getMe(1);
        assertEquals("reader1", profile.getUsername());
        assertEquals("Reader One", profile.getName());
    }

    @Test
    void getMe_shouldThrowWhenNotFound() {
        when(userMapper.findById(999)).thenReturn(null);
        assertThrows(AppException.class, () -> authService.getMe(999));
    }
}
