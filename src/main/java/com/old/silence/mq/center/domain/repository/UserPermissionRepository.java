package com.old.silence.mq.center.domain.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.old.silence.data.mybatis.projection.ProjectionMapperRepository;
import com.old.silence.mq.center.domain.model.UserPermission;

import java.math.BigInteger;

/**
 * 用户权限 Repository
 * 最常用于权限检查的查询
 */
public interface UserPermissionRepository extends ProjectionMapperRepository<UserPermission, BigInteger> {


    @Select("SELECT * FROM user_permission WHERE user_id = #{userId} AND topicId = ${topicId} AND permission_code = ${permissionCode}")
    UserPermission findByUserIdAndTopicIdAndPermissionCode(BigInteger userId, BigInteger topicId, String permissionCode);
}
