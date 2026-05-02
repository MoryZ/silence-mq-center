package com.old.silence.mq.center.domain.repository;

import org.apache.ibatis.annotations.Select;
import com.old.silence.data.mybatis.projection.ProjectionMapperRepository;
import com.old.silence.mq.center.domain.model.UserPermission;

import java.math.BigInteger;

/**
 * 用户权限 Repository
 * 最常用于权限检查的查询
 */
public interface UserPermissionRepository extends ProjectionMapperRepository<UserPermission, BigInteger> {


    @Select("SELECT * FROM rmq_user_permission WHERE user_name= #{userName} AND permission_code = ${permissionCode} AND is_expired = 0 ")
    UserPermission findByUsernameAndPermissionCode(String userName, String permissionCode);
}
