package com.old.silence.mq.center.domain.service;

import org.springframework.stereotype.Service;
import com.old.silence.core.context.CommonErrors;
import com.old.silence.mq.center.domain.model.UserPermission;
import com.old.silence.mq.center.domain.repository.UserPermissionRepository;
import com.old.silence.mq.center.enums.PermissionStatus;

import java.time.Instant;

/**
 * @author moryzang
 */
@Service
public class UserPermissionService {


    private final UserPermissionRepository userPermissionRepository;

    public UserPermissionService(UserPermissionRepository userPermissionRepository) {
        this.userPermissionRepository = userPermissionRepository;
    }

    public void grantPermission(String userName, String permissionCode, Instant expireTime) {
        var userPermission = userPermissionRepository.findByUsernameAndPermissionCode(userName, permissionCode);
        if (userPermission == null) {
            userPermission = new UserPermission();
            userPermission.setUserName(userName);
            userPermission.setPermissionCode(permissionCode);
            userPermission.setGrantedTime(Instant.now());
            userPermission.setExpireTime(expireTime);
            userPermission.setStatus(PermissionStatus.APPROVED);
            userPermissionRepository.insert(userPermission);
        }
        throw CommonErrors.FATAL_ERROR.createException("已拥有该权限");

    }

    public void revokePermission(String userName, String permissionCode) {
        var userPermission = userPermissionRepository.findByUsernameAndPermissionCode(userName, permissionCode);
        if (userPermission == null) {
            throw CommonErrors.FATAL_ERROR.createException("该用户未拥有对应权限");
        }
        userPermission.setStatus(PermissionStatus.WITHDRAWN);
        userPermissionRepository.updateNonNull(userPermission);
    }
}
