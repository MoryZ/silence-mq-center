-- ============================================================================
-- 权限控制系统数据库初始化脚本
-- ============================================================================
-- 执行顺序：permission_type → topic → permission_request → user_permission → permission_audit_log

-- ============================================================================
-- 1. 权限类型表
-- ============================================================================
DROP TABLE IF EXISTS `permission_type`;

CREATE TABLE `permission_type` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  `permission_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '权限代码：如 PRODUCE, CONSUME, CREATE_TOPIC',
  `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
  `description` VARCHAR(300) COMMENT '权限描述',
  `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
  `created_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY `idx_code` (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限类型字典表';

-- 初始数据
INSERT INTO `permission_type` (`permission_code`, `permission_name`, `description`, `status`) VALUES
('CREATE_TOPIC', '创建Topic', '允许创建新的Topic', 'ACTIVE'),
('DELETE_TOPIC', '删除Topic', '允许删除Topic', 'ACTIVE'),
('PRODUCE', '生产消息', '允许向Topic生产消息', 'ACTIVE'),
('CONSUME', '消费消息', '允许从Topic消费消息', 'ACTIVE'),
('MODIFY_TOPIC_CONFIG', '修改Topic配置', '允许修改Topic的配置', 'ACTIVE'),
('VIEW_TOPIC', '查看Topic', '允许查看Topic信息和统计', 'ACTIVE'),
('SUBSCRIBE_TOPIC', '订阅Topic', '允许创建消费者订阅Topic', 'ACTIVE'),
('MANAGE_ACL', '管理ACL', '允许管理Topic的ACL权限', 'ACTIVE'),
('RESET_OFFSET', '重置偏移量', '允许重置消费者偏移量', 'ACTIVE'),
('VIEW_MESSAGE', '查看消息', '允许查看Topic中的消息内容', 'ACTIVE');

-- ============================================================================
-- 2. Topic 表（RocketMQ Topic管理）
-- ============================================================================
DROP TABLE IF EXISTS `topic`;

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
  KEY `idx_owner_id` (`owner_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RocketMQ Topic表';

-- ============================================================================
-- 3. 权限申请表
-- ============================================================================
DROP TABLE IF EXISTS `permission_request`;

CREATE TABLE `permission_request` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '申请用户ID',
  `user_name` VARCHAR(100) NOT NULL COMMENT '申请用户名',
  `topic_id` BIGINT COMMENT '涉及的Topic ID（NULL表示全局权限）',
  `topic_name` VARCHAR(255) COMMENT 'Topic名称（冗余字段）',
  `permission_type_id` INT NOT NULL COMMENT '权限类型ID',
  `permission_code` VARCHAR(50) NOT NULL COMMENT '权限代码',
  `request_reason` VARCHAR(500) COMMENT '申请理由',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '申请状态：PENDING/APPROVED/REJECTED/EXPIRED/WITHDRAWN',
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
  KEY `idx_created_time` (`created_time`),
  FOREIGN KEY (`permission_type_id`) REFERENCES `permission_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限申请表';

-- ============================================================================
-- 4. 用户权限关系表（最终权限映射）
-- ============================================================================
DROP TABLE IF EXISTS `user_permission`;

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
  KEY `idx_status` (`status`),
  KEY `idx_expire_time` (`expire_time`),
  FOREIGN KEY (`permission_type_id`) REFERENCES `permission_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户权限关系表（权限的最终映射）';

-- ============================================================================
-- 5. 权限审计日志表
-- ============================================================================
DROP TABLE IF EXISTS `permission_audit_log`;

CREATE TABLE `permission_audit_log` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型：REQUEST/APPROVE/REJECT/REVOKE/GRANT/EXPIRE',
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
  KEY `idx_request_id` (`request_id`),
  KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限审计日志表';

-- ============================================================================
-- 索引优化
-- ============================================================================

-- 权限申请的查询优化
ALTER TABLE `permission_request` ADD INDEX `idx_user_status_time` (`user_id`, `status`, `created_time`);
ALTER TABLE `permission_request` ADD INDEX `idx_approver_status` (`approver_id`, `status`);

-- 用户权限的查询优化
ALTER TABLE `user_permission` ADD INDEX `idx_user_active` (`user_id`, `status`);
ALTER TABLE `user_permission` ADD INDEX `idx_permission_active` (`permission_type_id`, `status`);

-- ============================================================================
-- 初始化示例数据（可选）
-- ============================================================================

-- 示例数据：假设已有用户 user_id = 1, 2, 3
-- 示例Topic
INSERT INTO `topic` (`topic_name`, `cluster_name`, `description`, `owner_id`, `owner_name`) VALUES
('Order-Topic', 'DefaultCluster', '订单Topic', 1, 'admin'),
('Payment-Topic', 'DefaultCluster', '支付Topic', 1, 'admin'),
('User-Topic', 'DefaultCluster', '用户信息Topic', 1, 'admin');

-- 示例权限申请（已批准）
-- 系统A（用户ID 2）申请在Order-Topic上的生产权限
INSERT INTO `permission_request` 
(`user_id`, `user_name`, `topic_id`, `topic_name`, `permission_type_id`, `permission_code`, `request_reason`, `status`, `approver_id`, `approver_name`, `approval_time`)
VALUES 
(2, 'system_a', 1, 'Order-Topic', 3, 'PRODUCE', '系统A需要生产订单消息', 'APPROVED', 1, 'admin', NOW());

-- 系统B（用户ID 3）申请在Order-Topic上的消费权限
INSERT INTO `permission_request` 
(`user_id`, `user_name`, `topic_id`, `topic_name`, `permission_type_id`, `permission_code`, `request_reason`, `status`, `approver_id`, `approver_name`, `approval_time`)
VALUES 
(3, 'system_b', 1, 'Order-Topic', 4, 'CONSUME', '系统B需要消费订单消息', 'APPROVED', 1, 'admin', NOW());

-- 示例权限授予
INSERT INTO `user_permission` 
(`user_id`, `user_name`, `topic_id`, `topic_name`, `permission_type_id`, `permission_code`, `granted_by_id`, `granted_by_name`)
VALUES 
(2, 'system_a', 1, 'Order-Topic', 3, 'PRODUCE', 1, 'admin'),
(3, 'system_b', 1, 'Order-Topic', 4, 'CONSUME', 1, 'admin');

-- ============================================================================
-- END OF SCRIPT
-- ============================================================================
