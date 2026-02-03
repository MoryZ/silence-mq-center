package com.old.silence.mq.center.domain.model.permission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权限申请请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 申请用户ID
     */
    private Long userId;

    /**
     * 申请用户名
     */
    private String userName;

    /**
     * Topic ID（NULL表示全局权限）
     */
    private Long topicId;

    /**
     * Topic 名称
     */
    private String topicName;

    /**
     * 申请的权限代码
     */
    private String permissionCode;

    /**
     * 申请理由
     */
    private String reason;
}

/**
 * 权限批准请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ApprovePermissionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 申请ID
     */
    private Long requestId;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 审批人名称
     */
    private String approverName;

    /**
     * 批准理由
     */
    private String reason;

    /**
     * 权限过期时间
     */
    private LocalDateTime expireTime;
}

/**
 * 权限拒绝请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class RejectPermissionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 申请ID
     */
    private Long requestId;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 审批人名称
     */
    private String approverName;

    /**
     * 拒绝理由
     */
    private String reason;
}

/**
 * 权限查询响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class UserPermissionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 权限ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * Topic ID
     */
    private Long topicId;

    /**
     * Topic 名称
     */
    private String topicName;

    /**
     * 权限代码
     */
    private String permissionCode;

    /**
     * 权限名称
     */
    private String permissionName;

    /**
     * 授权人名称
     */
    private String grantedByName;

    /**
     * 授权时间
     */
    private LocalDateTime grantedTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 是否已过期
     */
    private Integer isExpired;

    /**
     * 权限状态
     */
    private String status;

    /**
     * 是否有效
     */
    private Boolean valid;
}

/**
 * 权限申请响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class PermissionRequestResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 申请ID
     */
    private Long id;

    /**
     * 申请用户ID
     */
    private Long userId;

    /**
     * 申请用户名
     */
    private String userName;

    /**
     * Topic ID
     */
    private Long topicId;

    /**
     * Topic 名称
     */
    private String topicName;

    /**
     * 权限代码
     */
    private String permissionCode;

    /**
     * 申请理由
     */
    private String requestReason;

    /**
     * 申请状态
     */
    private String status;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 审批人名称
     */
    private String approverName;

    /**
     * 审批意见
     */
    private String approvalReason;

    /**
     * 申请时间
     */
    private LocalDateTime createdTime;

    /**
     * 审批时间
     */
    private LocalDateTime approvalTime;

    /**
     * 权限过期时间
     */
    private LocalDateTime expireTime;
}

/**
 * API响应结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ApiResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 返回码
     */
    private Integer code;

    /**
     * 返回信息
     */
    private String message;

    /**
     * 返回数据
     */
    private Object data;

    /**
     * 时间戳
     */
    private Long timestamp;

    public static ApiResponse success(Object data) {
        return ApiResponse.builder()
                .success(true)
                .code(200)
                .message("success")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static ApiResponse error(Integer code, String message) {
        return ApiResponse.builder()
                .success(false)
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static ApiResponse error(String message) {
        return error(500, message);
    }
}
