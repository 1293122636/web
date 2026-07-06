package com.library.service;

import com.library.entity.User;
import com.library.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest extends AbstractServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userMapper);
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        when(userMapper.findAll()).thenReturn(List.of(createUser(1, "admin", "admin"), createUser(2, "reader1", "reader")));
        var result = userService.findAll();
        assertEquals(2, result.size());
    }

    @Test
    void findById_shouldReturnUser() {
        when(userMapper.findById(1)).thenReturn(createUser(1, "admin", "admin"));
        var result = userService.findById(1);
        assertEquals("admin", result.getUsername());
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(userMapper.findById(999)).thenReturn(null);
        assertThrows(AppException.class, () -> userService.findById(999));
    }

    @Test
    void update_shouldUpdateFields() {
        User user = createUser(1, "reader1", "reader");
        user.setName("OldName");
        user.setPhone("111");
        user.setEmail("old@test.com");
        when(userMapper.findById(1)).thenReturn(user);

        userService.update(1, "NewName", "222", "new@test.com");

        verify(userMapper).update(argThat(u ->
                "NewName".equals(u.getName()) &&
                "222".equals(u.getPhone()) &&
                "new@test.com".equals(u.getEmail())
        ));
    }

    @Test
    void update_shouldKeepNameWhenNull() {
        User user = createUser(1, "reader1", "reader");
        user.setName("Original");
        when(userMapper.findById(1)).thenReturn(user);

        userService.update(1, null, "111", "test@test.com");

        verify(userMapper).update(argThat(u -> "Original".equals(u.getName())));
    }

    @Test
    void update_shouldThrowWhenUserNotFound() {
        when(userMapper.findById(999)).thenReturn(null);
        assertThrows(AppException.class, () -> userService.update(999, "N", "1", "e@e.com"));
    }

    private User createUser(Integer id, String username, String role) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setRole(role);
        return u;
    }
}
