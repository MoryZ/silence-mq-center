package com.old.silence.mq.center.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.auth.center.security.SilenceAuthCenterContextHolder;
import com.old.silence.core.context.CommonErrors;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.mq.center.domain.model.PermissionRequest;
import com.old.silence.mq.center.domain.repository.PermissionAuditTaskRepository;
import com.old.silence.mq.center.domain.repository.PermissionRequestRepository;

import java.math.BigInteger;

/**
 *
 * @author moryzang
 * @since 2024-01-01
 */
@Service
public class PermissionRequestService {

    private static final Logger log = LoggerFactory.getLogger(PermissionRequestService.class);

    private final PermissionRequestRepository permissionRequestRepository;
    private final PermissionAuditTaskRepository permissionAuditTaskRepository;

    public PermissionRequestService(
            PermissionRequestRepository permissionRequestRepository, PermissionAuditTaskRepository permissionAuditTaskRepository) {
        this.permissionRequestRepository = permissionRequestRepository;
        this.permissionAuditTaskRepository = permissionAuditTaskRepository;
    }



    public IPage<PermissionRequest> findByQuery(QueryWrapper<PermissionRequest> queryWrapper, Page<PermissionRequest> page) {
        var authenticatedUserNameOptional = SilenceAuthCenterContextHolder.getAuthenticatedUserName();
        if (authenticatedUserNameOptional.isEmpty()) {
            throw CommonErrors.ACCESS_DENIED.createException("用户未登录");
        }
        queryWrapper.lambda().eq(PermissionRequest::getApplyName, authenticatedUserNameOptional.get());
        return permissionRequestRepository.selectPage(page, queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public BigInteger create(PermissionRequest permissionRequest) {
        permissionRequestRepository.insert(permissionRequest);
        var id = permissionRequest.getId();
        if(CollectionUtils.isEmpty(permissionRequest.getPermissionAuditTasks())) {
            return id;
        }
        BigInteger dependsOnAuditTaskId = null;
        for (var permissionAuditTask : permissionRequest.getPermissionAuditTasks()) {

            permissionAuditTask.setRequestId(id);
            if (dependsOnAuditTaskId != null) {
                permissionAuditTask.setDependsOnAuditTaskId(dependsOnAuditTaskId);
            }
            permissionAuditTaskRepository.insert(permissionAuditTask);

            dependsOnAuditTaskId = permissionAuditTask.getId();
        }

        return id;
    }
}
