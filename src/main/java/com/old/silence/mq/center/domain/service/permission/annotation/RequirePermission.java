package com.old.silence.mq.center.domain.service.permission.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限检查注解
 * 用于标注需要进行权限检查的方法
 * <p>
 * 使用示例：
 *
 * @RequirePermission(value = "PRODUCE", topicIdParamName = "topicId")
 * public void produceMessage(Long topicId, String message) {
 * // 方法体
 * }
 * <p>
 * 支持的参数获取方式：
 * 1. 直接从方法参数中获取 userId 和 topicId
 * 2. 支持自定义参数名称（通过 userIdParamName 和 topicIdParamName）
 * 3. 支持从 HttpRequest 或 SecurityContext 中获取当前用户ID
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 所需权限代码
     * 例如：PRODUCE, CONSUME, CREATE_TOPIC, DELETE_TOPIC
     */
    String value();

    /**
     * Topic ID 参数名称（可选）
     * 如果不指定，则默认查找参数名为 topicId 的参数
     * 如果为空字符串，表示不需要 topicId（全局权限检查）
     */
    String topicIdParamName() default "topicId";

    /**
     * 用户 ID 参数名称（可选）
     * 如果不指定，则默认从 SecurityContext 或 HttpRequest 中获取
     * 如果指定，则从方法参数中获取
     */
    String userIdParamName() default "";

    /**
     * 权限不足时是否记录日志
     */
    boolean logOnDeny() default true;

    /**
     * 权限检查失败时的错误消息（可选）
     * 如果为空，则使用默认错误消息
     */
    String errorMessage() default "";

    /**
     * 是否启用此权限检查（用于开发/测试环境禁用权限检查）
     */
    boolean enabled() default true;
}
