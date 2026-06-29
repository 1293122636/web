package com.library.service;

import com.library.mapper.*;
import com.library.util.JwtUtil;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Base class for all service unit tests.
 * Provides auto-mocked mappers and utilities.
 */
@ExtendWith(MockitoExtension.class)
public abstract class AbstractServiceTest {

    @Mock
    protected BookMapper bookMapper;

    @Mock
    protected BookItemMapper bookItemMapper;

    @Mock
    protected UserMapper userMapper;

    @Mock
    protected BorrowRecordMapper borrowRecordMapper;

    @Mock
    protected FineMapper fineMapper;

    @Mock
    protected HoldMapper holdMapper;

    @Mock
    protected CategoryMapper categoryMapper;

    @Mock
    protected CirculationRuleMapper circulationRuleMapper;

    @Mock
    protected PatronCategoryMapper patronCategoryMapper;

    @Mock
    protected ItemTypeMapper itemTypeMapper;

    @Mock
    protected AuditLogMapper auditLogMapper;

    @Mock
    protected JwtUtil jwtUtil;
}
