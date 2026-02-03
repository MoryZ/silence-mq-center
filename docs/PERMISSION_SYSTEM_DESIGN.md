# 权限控制系统设计

## 1. 系统概述

实现基于用户-权限-Topic的三维度权限管理系统，支持权限申请、审批、授予和检查。

```
集群（Cluster）
  └─ Topic A
      ├─ 系统/用户 1：CREATE_TOPIC、PRODUCE 权限
      └─ 系统/用户 2：CONSUME 权限
```

## 2. 数据库表结构设计

### 2.1 Topic 表（新增）

**目的**：记录RocketMQ中的Topic信息，支持权限管理

```sql
CREATE TABLE `topic` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  `topic_name` VARCHAR(255) NOT NULL UNIQUE COMMENT 'Topic名称',
  `cluster_name` VARCHAR(255) NOT NULL COMMENT '所属集群名称',
  `description` VARCHAR(500) COMMENT 'Topic描述',
  `owner_id` BIGINT COMMENT '所有者用户ID',
  `owner_name` VARCHAR(100) COMMENT '所有者名称',
  `is_system_topic` TINYINT(1) DEFAULT 0 COMMENT '是否系统Topic',
  `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/INACTIVE/DELETED',
  `created_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_cluster` (`cluster_name`),
  KEY `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RocketMQ Topic表';
```

---

### 2.2 权限类型表（新增）

**目的**：定义系统支持的所有权限类型

```sql
CREATE TABLE `permission_type` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  `permission_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '权限代码：如 PRODUCE, CONSUME, CREATE_TOPIC',
  `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
  `description` VARCHAR(300) COMMENT '权限描述',
  `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
  `created_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY `idx_code` (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限类型字典表';
```

**初始数据**：
```sql
INSERT INTO `permission_type` (`permission_code`, `permission_name`, `description`) VALUES
('CREATE_TOPIC', '创建Topic', '允许创建新的Topic'),
('DELETE_TOPIC', '删除Topic', '允许删除Topic'),
('PRODUCE', '生产消息', '允许向Topic生产消息'),
('CONSUME', '消费消息', '允许从Topic消费消息'),
('MODIFY_TOPIC_CONFIG', '修改Topic配置', '允许修改Topic的配置'),
('VIEW_TOPIC', '查看Topic', '允许查看Topic信息和统计'),
('SUBSCRIBE_TOPIC', '订阅Topic', '允许创建消费者订阅Topic');
```

---

### 2.3 权限申请表（新增）

**目的**：记录用户的权限申请和审批历史

```sql
CREATE TABLE `permission_request` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '申请用户ID',
  `user_name` VARCHAR(100) NOT NULL COMMENT '申请用户名',
  `topic_id` BIGINT NOT NULL COMMENT '涉及的Topic ID（NULL表示全局权限）',
  `topic_name` VARCHAR(255) COMMENT 'Topic名称（冗余字段）',
  `permission_type_id` INT NOT NULL COMMENT '权限类型ID',
  `permission_code` VARCHAR(50) NOT NULL COMMENT '权限代码',
  `request_reason` VARCHAR(500) COMMENT '申请理由',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '申请状态：PENDING/APPROVED/REJECTED/EXPIRED',
  `approver_id` BIGINT COMMENT '审批人ID',
  `approver_name` VARCHAR(100) COMMENT '审批人名称',
  `approval_reason` VARCHAR(500) COMMENT '审批意见',
  `approval_time` TIMESTAMP NULL COMMENT '审批时间',
  `expire_time` TIMESTAMP NULL COMMENT '权限过期时间',
  `created_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `updated_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_user_id` (`user_id`),
  KEY `idx_topic_id` (`topic_id`),
  KEY `idx_permission_type` (`permission_type_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限申请表';
```

---

### 2.4 用户权限关系表（新增）

**目的**：记录已批准的用户权限（权限的最终映射）

```sql
CREATE TABLE `user_permission` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `user_name` VARCHAR(100) NOT NULL COMMENT '用户名',
  `topic_id` BIGINT COMMENT 'Topic ID（NULL表示全局权限）',
  `topic_name` VARCHAR(255) COMMENT 'Topic名称（冗余字段）',
  `permission_type_id` INT NOT NULL COMMENT '权限类型ID',
  `permission_code` VARCHAR(50) NOT NULL COMMENT '权限代码',
  `granted_by_id` BIGINT COMMENT '授权人ID',
  `granted_by_name` VARCHAR(100) COMMENT '授权人名称',
  `granted_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
  `expire_time` TIMESTAMP NULL COMMENT '权限过期时间（NULL表示永久）',
  `is_expired` TINYINT(1) DEFAULT 0 COMMENT '是否已过期',
  `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/REVOKED/EXPIRED',
  `updated_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_user_topic_perm` (`user_id`, `topic_id`, `permission_type_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_topic_id` (`topic_id`),
  KEY `idx_permission_type` (`permission_type_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户权限关系表';
```

---

### 2.5 权限审计日志表（新增）

**目的**：记录所有权限相关的操作日志，用于审计和追踪

```sql
CREATE TABLE `permission_audit_log` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型：REQUEST/APPROVE/REJECT/REVOKE/GRANT',
  `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(100) NOT NULL COMMENT '操作人名称',
  `target_user_id` BIGINT COMMENT '目标用户ID',
  `target_user_name` VARCHAR(100) COMMENT '目标用户名',
  `topic_id` BIGINT COMMENT 'Topic ID',
  `topic_name` VARCHAR(255) COMMENT 'Topic名称',
  `permission_code` VARCHAR(50) COMMENT '权限代码',
  `request_id` BIGINT COMMENT '关联的权限申请ID',
  `operation_details` JSON COMMENT '操作详情（JSON格式）',
  `operation_result` VARCHAR(20) DEFAULT 'SUCCESS' COMMENT '操作结果：SUCCESS/FAILED',
  `error_message` VARCHAR(500) COMMENT '错误信息',
  `ip_address` VARCHAR(50) COMMENT '操作人IP',
  `created_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_target_user_id` (`target_user_id`),
  KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限审计日志表';
```

---

## 3. 表之间的关系

```
用户表 (user)
  │
  ├─→ Topic表
  │    │
  │    ├─→ PermissionRequest (权限申请)
  │    │    └─→ PermissionType (权限类型)
  │    │
  │    └─→ UserPermission (最终权限)
  │         └─→ PermissionType (权限类型)
  │
  └─→ PermissionAuditLog (审计日志)
```

---

## 4. 权限检查流程

```
用户请求操作 (如创建Topic)
  │
  ├─→ 检查用户是否存在
  │
  ├─→ 查询 user_permission 表
  │    WHERE user_id = ? AND permission_code = ? AND status = 'ACTIVE' 
  │         AND (expire_time IS NULL OR expire_time > NOW())
  │
  ├─→ 权限存在且未过期？
  │    ├─→ YES: 允许操作
  │    └─→ NO: 返回权限不足错误
  │
  └─→ 记录到 permission_audit_log 表
```

---

## 5. 业务流程

### 5.1 权限申请流程

```
用户申请权限
  └─→ 创建 PermissionRequest (状态: PENDING)
      └─→ 通知管理员审批
```

### 5.2 权限审批流程

```
管理员审批
  ├─→ 审核申请内容
  ├─→ 更新 PermissionRequest (状态: APPROVED/REJECTED)
  ├─→ 如果批准，创建 UserPermission 记录
  └─→ 记录 PermissionAuditLog
```

### 5.3 权限查询流程

```
用户查询权限
  └─→ 查询 UserPermission 表
      └─→ 返回所有有效权限
```

---

## 6. SQL创建脚本汇总

见下一节：`permission_schema.sql`

