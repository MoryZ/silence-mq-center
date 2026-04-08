package com.old.silence.mq.center.api.util;

import org.springframework.stereotype.Component;

import com.old.silence.mq.center.domain.model.PermissionRequest;
import com.old.silence.mq.center.domain.model.UserPermission;
import com.old.silence.mq.center.dto.UserPermissionDTO;
import com.old.silence.mq.center.vo.PermissionRequestResponseDTO;

/**
 * 权限相关的DTO转换工具
 * <p>
 * 负责Entity与DTO之间的转换，集中管理转换逻辑
 * 
 * @author Silence
 * @since 2024-01-01
 */
@Component
public class PermissionDTOConverter {

    /**
     * 将PermissionRequest实体转换为PermissionRequestResponseDTO
     *
     * @param request 权限申请实体
     * @return 权限申请响应DTO
     */
    public PermissionRequestResponseDTO toRequestResponseDTO(PermissionRequest request) {
        if (request == null) {
            return null;
        }

        PermissionRequestResponseDTO dto = new PermissionRequestResponseDTO();
        dto.setRequestId(request.getId());
        dto.setUserId(request.getUserId());
        dto.setUserName(request.getUserName());
        dto.setTopicId(request.getTopicId());
        dto.setPermissionCode(request.getPermissionCode());
        dto.setRequestReason(request.getRequestReason());
        dto.setStatus(request.getStatus());
        dto.setApprovalReason(request.getApprovalReason());
        
        return dto;
    }

    /**
     * 将UserPermission实体转换为UserPermissionDTO
     *
     * @param permission 用户权限实体
     * @return 用户权限DTO
     */
    public UserPermissionDTO toUserPermissionDTO(UserPermission permission) {
        if (permission == null) {
            return null;
        }

        UserPermissionDTO dto = new UserPermissionDTO();
        dto.setPermissionId(permission.getId());
        dto.setUserId(permission.getUserId());
        dto.setUserName(permission.getUserName());
        dto.setTopicId(permission.getTopicId());
        dto.setPermissionCode(permission.getPermissionCode());
        dto.setStatus(permission.getStatus());
        dto.setExpireTime(permission.getExpireTime());
        dto.setGrantedTime(permission.getGrantedTime());
        dto.setExpired(permission.getExpired());
        
        return dto;
    }
}
