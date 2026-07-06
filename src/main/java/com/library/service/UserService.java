package com.library.service;

import com.library.entity.User;
import com.library.exception.AppException;
import com.library.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public List<User> findAll() {
        return userMapper.findAll();
    }

    public User findById(Integer id) {
        User user = userMapper.findById(id);
        if (user == null) throw AppException.notFound("User not found");
        return user;
    }

    @Transactional
    public void update(Integer id, String name, String phone, String email) {
        User user = userMapper.findById(id);
        if (user == null) throw AppException.notFound("User not found");
        user.setName(name != null ? name : user.getName());
        user.setPhone(phone);
        user.setEmail(email);
        userMapper.update(user);
    }
}
