package com.old.silence.mq.center.domain.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import com.old.silence.data.mybatis.projection.ProjectionMapperRepository;
import com.old.silence.mq.center.domain.model.PermissionRequest;
import com.old.silence.mq.center.enums.PermissionStatus;

import java.math.BigInteger;

/**
 * 权限申请 Repository
 */
@Mapper
public interface PermissionRequestRepository extends ProjectionMapperRepository<PermissionRequest, BigInteger> {

    @Update("UPDATE rmq_permission_request SET status = #{permissionStatus.value} WHERE id = #{id} ")
    int updateStatusById(PermissionStatus permissionStatus, BigInteger id);
}
