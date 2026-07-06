package com.library.controller;

import com.library.dto.request.LoginRequest;
import com.library.dto.request.RegisterRequest;
import com.library.dto.response.LoginResponse;
import com.library.dto.response.UserProfile;
import com.library.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(data));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest data) {
        return ResponseEntity.ok(authService.login(data.getUsername(), data.getPassword()));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfile> getMe(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return ResponseEntity.ok(authService.getMe(userId));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserProfile>> listUsers() {
        return ResponseEntity.ok(authService.listUsers());
    }

    @PostMapping("/admin/create")
    public ResponseEntity<UserProfile> createAdmin(@Valid @RequestBody RegisterRequest data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createAdmin(data));
    }
}
