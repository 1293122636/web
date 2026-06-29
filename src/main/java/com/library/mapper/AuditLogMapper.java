package com.library.mapper;

import com.library.entity.AuditLog;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AuditLogMapper {

    @Insert("INSERT INTO audit_logs(user_id, action, target, detail, created_at) VALUES(#{userId}, #{action}, #{target}, #{detail}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AuditLog log);
}
