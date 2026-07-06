package com.library.service;

import com.library.dto.request.RegisterRequest;
import com.library.dto.response.LoginResponse;
import com.library.dto.response.UserProfile;
import com.library.entity.AuditLog;
import com.library.entity.User;
import com.library.exception.AppException;
import com.library.mapper.AuditLogMapper;
import com.library.mapper.UserMapper;
import com.library.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditLogMapper auditLogMapper;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, AuditLogMapper auditLogMapper) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.auditLogMapper = auditLogMapper;
    }

    @Transactional
    public LoginResponse register(RegisterRequest data) {
        User user = new User();
        user.setUsername(data.getUsername());
        user.setPassword(passwordEncoder.encode(data.getPassword()));
        user.setName(data.getName());
        user.setRole("reader");
        user.setPhone(data.getPhone());
        user.setEmail(data.getEmail());
        user.setTotalFines(java.math.BigDecimal.ZERO);
        try {
            userMapper.insert(user);
        } catch (DataIntegrityViolationException e) {
            throw AppException.conflict("用户名已存在");
        }

        UserProfile profile = toProfile(user);
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        audit("register", "user:" + user.getId(), "Registered user: " + user.getUsername());
        return new LoginResponse(profile, token);
    }

    public LoginResponse login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw AppException.unauthorized("用户名或密码错误");
        }

        UserProfile profile = toProfile(user);
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        return new LoginResponse(profile, token);
    }

    public UserProfile getMe(Integer userId) {
        User user = userMapper.findById(userId);
        if (user == null) throw AppException.notFound("用户不存在");
        return toProfile(user);
    }

    public List<UserProfile> listUsers() {
        return userMapper.findAll().stream()
                .map(this::toProfile)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public UserProfile createAdmin(RegisterRequest data) {
        User user = new User();
        user.setUsername(data.getUsername());
        user.setPassword(passwordEncoder.encode(data.getPassword()));
        user.setName(data.getName());
        user.setRole("admin");
        user.setPhone(data.getPhone());
        user.setEmail(data.getEmail());
        user.setTotalFines(java.math.BigDecimal.ZERO);
        try {
            userMapper.insert(user);
        } catch (DataIntegrityViolationException e) {
            throw AppException.conflict("用户名已存在");
        }

        audit("createAdmin", "user:" + user.getId(), "Created admin: " + user.getUsername());
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

    private void audit(String action, String target, String detail) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setTarget(target);
        auditLog.setDetail(detail);
        try {
            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            log.error("审计日志写入失败: action={}, target={}", action, target, e);
        }
    }
}
