package com.old.silence.mq.center.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.old.silence.mq.center.domain.service.PermissionAuditTaskService;
import com.old.silence.mq.center.dto.ApprovePermissionCommand;
import com.old.silence.mq.center.dto.RejectPermissionCommand;
import com.old.silence.mq.center.exception.PermissionDeniedException;

/**
 *
 * @author moryzang
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/v1")
public class PermissionAuditTaskController {

    private static final Logger log = LoggerFactory.getLogger(PermissionAuditTaskController.class);

    private final PermissionAuditTaskService permissionAuditTaskService;

    public PermissionAuditTaskController(PermissionAuditTaskService permissionAuditTaskService) {
        this.permissionAuditTaskService = permissionAuditTaskService;
    }

    /**
     * 审批权限申请（通过）
     * <p>
     * 管理员同意申请者的权限申请，会为申请者创建有效期内的权限
     *
     * @param approvePermissionCommand 审批请求
     */
    @PutMapping("/permissionAuditTasks/approve")
    public void approvePermission(@RequestBody ApprovePermissionCommand approvePermissionCommand) throws PermissionDeniedException {
        try {

            permissionAuditTaskService.approvePermission(
                    approvePermissionCommand.getPermissionAuditTaskId(),
                    approvePermissionCommand.getApproverName(),
                    approvePermissionCommand.getApprovalReason()
            );
        } catch (PermissionDeniedException e) {
            log.warn("审批权限申请被拒绝: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("审批权限申请失败", e);
            throw e;
        }
    }

    /**
     * 拒绝权限申请
     * <p>
     * 管理员拒绝申请者的权限申请，申请会进入拒绝状态
     *
     * @param rejectPermissionCommand 拒绝请求
     */
    @PutMapping("/permissionAuditTasks/reject")
    public void rejectPermission(@RequestBody RejectPermissionCommand rejectPermissionCommand) {
        try {
            permissionAuditTaskService.rejectPermission(
                    rejectPermissionCommand.getPermissionAuditTaskId(),
                    rejectPermissionCommand.getApproverName(),
                    rejectPermissionCommand.getRejectionReason()
            );
        } catch (Exception e) {
            log.error("拒绝权限申请失败", e);
            throw e;
        }
    }
}
