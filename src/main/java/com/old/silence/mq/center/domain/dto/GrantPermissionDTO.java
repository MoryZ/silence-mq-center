package com.old.silence.mq.center.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 赋予权限 DTO
 * 
 * 用于管理员直接为用户赋予权限的请求
 * 
 * @author Silence
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrantPermissionDTO {
    
    /**
     * 要赋予权限的用户 ID
     */
    private Long userId;
    
    /**
     * 用户名称
     */
    private String userName;
    
    /**
     * Topic ID（为 null 表示赋予全局权限）
     */
    private Long topicId;
    
    /**
     * 权限代码（如：PRODUCE、CONSUME、CREATE_TOPIC）
     */
    private String permissionCode;
    
    /**
     * 权限过期时间（为 null 表示永不过期）
     */
    private LocalDateTime expireTime;
}
