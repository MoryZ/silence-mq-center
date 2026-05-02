package com.old.silence.mq.center.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.type.TypeReference;
import com.old.silence.core.context.CommonErrors;
import com.old.silence.core.exception.ResourceNotFoundException;
import com.old.silence.json.JacksonMapper;
import com.old.silence.mq.center.domain.repository.PermissionAuditTaskRepository;
import com.old.silence.mq.center.domain.repository.PermissionRequestRepository;
import com.old.silence.mq.center.enums.OperationType;
import com.old.silence.mq.center.enums.PermissionStatus;

import java.math.BigInteger;
import java.util.Map;

/**
 * @author moryzang
 */
@Service
public class PermissionAuditTaskService {

    private final PermissionRequestRepository permissionRequestRepository;
    private final PermissionAuditTaskRepository permissionAuditTaskRepository;
    private final JacksonMapper jacksonMapper;

    public PermissionAuditTaskService(PermissionRequestRepository permissionRequestRepository,
                                      PermissionAuditTaskRepository permissionAuditTaskRepository,
                                      JacksonMapper jacksonMapper) {
        this.permissionRequestRepository = permissionRequestRepository;
        this.permissionAuditTaskRepository = permissionAuditTaskRepository;
        this.jacksonMapper = jacksonMapper;
    }


    public int approvePermission(BigInteger permissionAuditTaskId, String approverName, String approvalReason) {
        var permissionAuditTask = permissionAuditTaskRepository.findById(permissionAuditTaskId)
                .orElseThrow(ResourceNotFoundException::new);

        if (hasPassedPrevAuditTask(permissionAuditTask.getDependsOnAuditTaskId())) {
            throw CommonErrors.FATAL_ERROR.createException("前置任务未完成");
        }
        permissionAuditTask.setOperatorName(approverName);
        permissionAuditTask.setOperationType(OperationType.APPROVE);
        permissionAuditTask.setOperationDetails(jacksonMapper.unwrap().convertValue(approvalReason, new TypeReference<Map<String, Object>>() {}));

        return permissionAuditTaskRepository.updateNonNull(permissionAuditTask);
    }

    @Transactional
    public int rejectPermission(BigInteger permissionAuditTaskId, String approverName, String rejectionReason) {
        var permissionAuditTask = permissionAuditTaskRepository.findById(permissionAuditTaskId)
                .orElseThrow(ResourceNotFoundException::new);

        if (hasPassedPrevAuditTask(permissionAuditTask.getDependsOnAuditTaskId())) {
            throw CommonErrors.FATAL_ERROR.createException("前置任务未完成");
        }

        permissionAuditTask.setOperatorName(approverName);
        permissionAuditTask.setOperationType(OperationType.REJECT);
        permissionAuditTask.setOperationDetails(jacksonMapper.unwrap().convertValue(rejectionReason, new TypeReference<Map<String, Object>>() {}));

        var rowsAffected = permissionAuditTaskRepository.updateNonNull(permissionAuditTask);
        permissionRequestRepository.updateStatusById(PermissionStatus.REJECTED, permissionAuditTask.getRequestId());
        return rowsAffected;

    }

    private boolean hasPassedPrevAuditTask(BigInteger permissionAuditTaskId) {
        return permissionAuditTaskRepository.findById(permissionAuditTaskId)
                .map(permissionAuditTask -> OperationType.APPROVE.equals(permissionAuditTask.getOperationType()))
                .isPresent();
    }
}
