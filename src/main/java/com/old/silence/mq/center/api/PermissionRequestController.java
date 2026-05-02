package com.old.silence.mq.center.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.data.commons.converter.QueryWrapperConverter;
import com.old.silence.mq.center.api.assembler.PermissionRequestMapper;
import com.old.silence.mq.center.domain.model.PermissionRequest;
import com.old.silence.mq.center.domain.service.PermissionRequestService;
import com.old.silence.mq.center.dto.PermissionRequestCommand;
import com.old.silence.mq.center.dto.PermissionRequestQuery;

import java.math.BigInteger;

/**
 *
 * @author moryzang
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/v1")
public class PermissionRequestController {

    private static final Logger log = LoggerFactory.getLogger(PermissionRequestController.class);

    private final PermissionRequestService permissionRequestService;
    private final PermissionRequestMapper permissionRequestMapper;

    public PermissionRequestController(
            PermissionRequestService permissionRequestService,
            PermissionRequestMapper permissionRequestMapper) {
        this.permissionRequestService = permissionRequestService;
        this.permissionRequestMapper = permissionRequestMapper;
    }

    /**
     * 获取权限申请列表
     */
    @GetMapping(value = "/permissionRequests", params = {"pageNo", "pageSize"})
    public IPage<PermissionRequest> query(PermissionRequestQuery permissionRequestQuery,
                                                       Page<PermissionRequest> page) {
        var queryWrapper = QueryWrapperConverter.convert(permissionRequestQuery, PermissionRequest.class);
        return permissionRequestService.findByQuery(queryWrapper, page);
    }


    @PostMapping("/permissionRequests")
    public BigInteger create(@RequestBody PermissionRequestCommand command) {
        try {
            var permissionRequest = permissionRequestMapper.convert(command);
            return permissionRequestService.create(permissionRequest);
        } catch (Exception e) {
            log.error("权限申请失败", e);
            throw e;
        }
    }

}
