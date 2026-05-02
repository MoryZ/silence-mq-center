package com.old.silence.mq.center.api.assembler;

import org.jetbrains.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;
import com.old.silence.core.mapstruct.MapStructSpringConfig;
import com.old.silence.mq.center.domain.model.PermissionAuditTask;
import com.old.silence.mq.center.domain.model.PermissionRequest;
import com.old.silence.mq.center.dto.PermissionRequestCommand;
import com.old.silence.mq.center.enums.OperationType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author moryzang
 */
@Mapper(uses = MapStructSpringConfig.class)
public interface PermissionRequestMapper extends Converter<PermissionRequestCommand, PermissionRequest> {

    @Nullable
    @Override
    @Mapping(target = "status", constant = "PENDING" )
    @Mapping(target = "permissionAuditTasks", expression = "java(toPermissionAuditTasks(command))")
    PermissionRequest convert(PermissionRequestCommand command);


    default List<PermissionAuditTask> toPermissionAuditTasks(PermissionRequestCommand command) {
        var auditors = command.getAuditChain().split(",");
        List<PermissionAuditTask> permissionAuditTasks = new ArrayList<>();
        for (var auditor : auditors) {
            PermissionAuditTask permissionAuditTask = new PermissionAuditTask();
            permissionAuditTask.setOperatorName(auditor);
            permissionAuditTask.setOperationType(OperationType.GRANT);
            permissionAuditTasks.add(permissionAuditTask);
        }
        return permissionAuditTasks;
    }
}
