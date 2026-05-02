-- Mock data seeds

INSERT IGNORE INTO `rmq_permission_type` (`permission_code`, `permission_name`, `description`, `status`)
VALUES ('CREATE_TOPIC', '创建Topic', '允许创建新的Topic', 'ACTIVE'),
       ('DELETE_TOPIC', '删除Topic', '允许删除Topic', 'ACTIVE'),
       ('PRODUCE', '生产消息', '允许向Topic生产消息', 'ACTIVE'),
       ('CONSUME', '消费消息', '允许从Topic消费消息', 'ACTIVE'),
       ('MODIFY_TOPIC_CONFIG', '修改Topic配置', '允许修改Topic的配置', 'ACTIVE'),
       ('VIEW_TOPIC', '查看Topic', '允许查看Topic信息和统计', 'ACTIVE'),
       ('SUBSCRIBE_TOPIC', '订阅Topic', '允许创建消费者订阅Topic', 'ACTIVE'),
       ('MANAGE_ACL', '管理ACL', '允许管理Topic的ACL权限', 'ACTIVE'),
       ('RESET_OFFSET', '重置偏移量', '允许重置消费者偏移量', 'ACTIVE'),
       ('VIEW_MESSAGE', '查看消息', '允许查看Topic中的消息内容', 'ACTIVE');

INSERT IGNORE INTO `rmq_topic`
(`topic_name`, `cluster_name`, `description`, `owner_id`, `owner_name`, `read_queue_nums`, `write_queue_nums`,
 `message_type`, `broker_addr`, `is_system_topic`, `status`)
VALUES ('Order-Topic', 'DefaultCluster', '订单Topic', 1, 'admin', 8, 8, 'NORMAL', '127.0.0.1:10911', 0, 'ACTIVE'),
       ('Payment-Topic', 'DefaultCluster', '支付Topic', 1, 'admin', 8, 8, 'NORMAL', '127.0.0.1:10911', 0, 'ACTIVE'),
       ('User-Topic', 'DefaultCluster', '用户信息Topic', 1, 'admin', 8, 8, 'NORMAL', '127.0.0.1:10911', 0, 'ACTIVE');

INSERT IGNORE INTO `rmq_permission_request`
(`user_id`, `user_name`, `topic_id`, `topic_name`, `permission_type_id`, `permission_code`, `request_reason`, `status`,
 `approver_id`, `approver_name`, `approval_time`)
VALUES (2, 'system_a', 1, 'Order-Topic', 3, 'PRODUCE', '系统A需要生产订单消息', 'APPROVED', 1, 'admin', NOW()),
       (3, 'system_b', 1, 'Order-Topic', 4, 'CONSUME', '系统B需要消费订单消息', 'APPROVED', 1, 'admin', NOW());

INSERT IGNORE INTO `rmq_user_permission`
(`user_id`, `user_name`, `topic_id`, `topic_name`, `permission_type_id`, `permission_code`, `granted_by_id`,
 `granted_by_name`, `status`)
VALUES (2, 'system_a', 1, 'Order-Topic', 3, 'PRODUCE', 1, 'admin', 'ACTIVE'),
       (3, 'system_b', 1, 'Order-Topic', 4, 'CONSUME', 1, 'admin', 'ACTIVE');

INSERT IGNORE INTO `rmq_consumer_monitor`
(`group_name`, `min_count`, `max_diff_total`, `created_by`, `updated_by`)
VALUES ('CONTENT_GROUP', 10, 500, 'system', 'system');
