package com.old.silence.mq.center.domain.service.permission.example;

import com.old.silence.mq.center.domain.service.permission.annotation.RequirePermission;
import org.springframework.stereotype.Service;

/**
 * 权限检查拦截器使用示例
 * 
 * 演示如何在 Service 中使用 @RequirePermission 注解
 * 无需修改方法内部逻辑，仅通过注解声明权限需求
 */
@Service
public class PermissionCheckExample {

    // ==================== 示例1：全局权限检查（无需 topicId） ====================

    /**
     * 创建 Topic（需要 CREATE_TOPIC 权限）
     * 权限检查会自动从 SecurityContext 获取当前用户ID
     */
    @RequirePermission("CREATE_TOPIC")
    public void createTopic(String topicName, String clusterName) {
        System.out.println("创建 Topic: " + topicName);
        // 方法体保持不变
        // 权限检查在方法执行前自动进行
    }

    /**
     * 删除 Topic（需要 DELETE_TOPIC 权限）
     */
    @RequirePermission("DELETE_TOPIC")
    public void deleteTopic(String topicName) {
        System.out.println("删除 Topic: " + topicName);
    }

    // ==================== 示例2：Topic 级权限检查 ====================

    /**
     * 生产消息（需要在指定 Topic 上的 PRODUCE 权限）
     * topicIdParamName = "topicId" 告诉 AOP 从参数名为 topicId 的参数中获取 Topic ID
     */
    @RequirePermission(value = "PRODUCE", topicIdParamName = "topicId")
    public void produceMessage(Long topicId, String message) {
        System.out.println("生产消息 - Topic: " + topicId + ", Message: " + message);
    }

    /**
     * 消费消息（需要在指定 Topic 上的 CONSUME 权限）
     */
    @RequirePermission(value = "CONSUME", topicIdParamName = "topicId")
    public void consumeMessage(Long topicId, String consumerGroup) {
        System.out.println("消费消息 - Topic: " + topicId + ", Group: " + consumerGroup);
    }

    /**
     * 修改 Topic 配置（需要在指定 Topic 上的 MODIFY_TOPIC_CONFIG 权限）
     */
    @RequirePermission(value = "MODIFY_TOPIC_CONFIG", topicIdParamName = "topicId")
    public void modifyTopicConfig(Long topicId, String configKey, String configValue) {
        System.out.println("修改 Topic 配置 - Topic: " + topicId + ", Key: " + configKey);
    }

    // ==================== 示例3：自定义参数名称 ====================

    /**
     * 查看消息详情（需要 VIEW_MESSAGE 权限）
     * 使用自定义的 Topic ID 参数名称
     */
    @RequirePermission(value = "VIEW_MESSAGE", topicIdParamName = "tid")
    public void viewMessage(Long tid, String messageId) {
        System.out.println("查看消息 - Topic: " + tid + ", MessageId: " + messageId);
    }

    /**
     * 查看消息轨迹（需要 VIEW_MESSAGE 权限）
     */
    @RequirePermission(value = "VIEW_MESSAGE", topicIdParamName = "topic_id")
    public void viewMessageTrace(Long topic_id, String messageId) {
        System.out.println("查看消息轨迹 - Topic: " + topic_id + ", MessageId: " + messageId);
    }

    // ==================== 示例4：自定义用户 ID 参数名称 ====================

    /**
     * 重置消费者偏移量（从参数中获取 userId 和 topicId）
     * userIdParamName = "userId" 告诉 AOP 从参数名为 userId 的参数中获取用户ID
     */
    @RequirePermission(value = "RESET_OFFSET", userIdParamName = "userId", topicIdParamName = "topicId")
    public void resetConsumerOffset(Long userId, Long topicId, String consumerGroup) {
        System.out.println("重置偏移量 - User: " + userId + ", Topic: " + topicId);
    }

    // ==================== 示例5：自定义错误消息 ====================

    /**
     * 管理 ACL（权限检查失败时显示自定义错误消息）
     */
    @RequirePermission(
        value = "MANAGE_ACL",
        topicIdParamName = "topicId",
        errorMessage = "您没有权限管理此 Topic 的 ACL，请联系管理员"
    )
    public void manageAcl(Long topicId, String accessKey, String permissions) {
        System.out.println("管理 ACL - Topic: " + topicId);
    }

    // ==================== 示例6：开发/测试模式（禁用权限检查） ====================

    /**
     * 测试数据生产（可以在开发环境禁用权限检查）
     */
    @RequirePermission(
        value = "PRODUCE",
        topicIdParamName = "topicId",
        enabled = false  // 设置为 false 可以在开发环境禁用此权限检查
    )
    public void produceTestData(Long topicId, String testData) {
        System.out.println("生产测试数据 - Topic: " + topicId);
    }

    // ==================== 示例7：禁用日志输出 ====================

    /**
     * 批量生产消息（权限检查失败时不记录日志）
     */
    @RequirePermission(
        value = "PRODUCE",
        topicIdParamName = "topicId",
        logOnDeny = false  // 权限不足时不记录 WARN 日志
    )
    public void batchProduceMessage(Long topicId, java.util.List<String> messages) {
        System.out.println("批量生产消息 - Topic: " + topicId);
    }
}
