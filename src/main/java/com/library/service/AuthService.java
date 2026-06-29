package com.library.service;

import com.library.dto.request.RegisterRequest;
import com.library.dto.response.LoginResponse;
import com.library.dto.response.UserProfile;
import com.library.entity.User;
import com.library.exception.AppException;
import com.library.mapper.UserMapper;
import com.library.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse register(RegisterRequest data) {
        User existing = userMapper.findByUsername(data.getUsername());
        if (existing != null) {
            throw AppException.conflict("Username already exists");
        }

        User user = new User();
        user.setUsername(data.getUsername());
        user.setPassword(passwordEncoder.encode(data.getPassword()));
        user.setName(data.getName());
        user.setRole("reader");
        user.setPhone(data.getPhone());
        user.setEmail(data.getEmail());
        user.setTotalFines(java.math.BigDecimal.ZERO);
        userMapper.insert(user);

        UserProfile profile = toProfile(user);
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        return new LoginResponse(profile, token);
    }

    public LoginResponse login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw AppException.unauthorized("Invalid credentials");
        }

        UserProfile profile = toProfile(user);
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        return new LoginResponse(profile, token);
    }

    public UserProfile getMe(Integer userId) {
        User user = userMapper.findById(userId);
        if (user == null) throw AppException.notFound("User not found");
        return toProfile(user);
    }

    public List<User> listUsers() {
        return userMapper.findAll();
    }

    public UserProfile createAdmin(RegisterRequest data) {
        User existing = userMapper.findByUsername(data.getUsername());
        if (existing != null) throw AppException.conflict("Username exists");

        User user = new User();
        user.setUsername(data.getUsername());
        user.setPassword(passwordEncoder.encode(data.getPassword()));
        user.setName(data.getName());
        user.setRole("admin");
        user.setPhone(data.getPhone());
        user.setEmail(data.getEmail());
        user.setTotalFines(java.math.BigDecimal.ZERO);
        userMapper.insert(user);

        return toProfile(user);
    }

    private UserProfile toProfile(User user) {
        UserProfile p = new UserProfile();
        p.setId(user.getId());
        p.setUsername(user.getUsername());
        p.setName(user.getName());
        p.setRole(user.getRole());
        p.setPhone(user.getPhone());
        p.setEmail(user.getEmail());
        p.setPatronCategoryId(user.getPatronCategoryId());
        p.setTotalFines(user.getTotalFines() != null ? user.getTotalFines().doubleValue() : 0);
        p.setCreatedAt(user.getCreatedAt());
        return p;
    }
}
