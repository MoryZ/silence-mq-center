package com.old.silence.mq.center.domain.service.permission.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.old.silence.mq.center.domain.service.permission.PermissionService;
import com.old.silence.mq.center.domain.service.permission.annotation.RequirePermission;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.math.BigInteger;

/**
 * 权限检查切面 (AOP)
 * <p>
 * 用于拦截标注了 @RequirePermission 的方法，在方法执行前进行权限检查
 * <p>
 * 特点：
 * 1. 侵入性低 - 不需要修改现有 Service 代码
 * 2. 声明式权限检查 - 只需添加注解即可
 * 3. 自动获取用户信息 - 从 SecurityContext 或请求中自动获取
 * 4. 灵活的参数获取 - 支持多种参数名称和位置
 * <p>
 * 使用示例：
 *
 * @Service public class TopicServiceImpl {
 * @RequirePermission("CREATE_TOPIC") public void createTopic(Topic topic) {
 * // 自动检查当前用户是否有 CREATE_TOPIC 权限
 * }
 * @RequirePermission(value = "PRODUCE", topicIdParamName = "topicId")
 * public void produceMessage(Long topicId, String message) {
 * // 自动检查当前用户在 topicId 上是否有 PRODUCE 权限
 * }
 * @RequirePermission(value = "CONSUME", userIdParamName = "userId", topicIdParamName = "topicId")
 * public void consumeMessage(Long userId, Long topicId) {
 * // 从方法参数中获取 userId 和 topicId，然后检查权限
 * }
 * }
 */
@Aspect
@Component
public class PermissionCheckAspect {

    private static final Logger logger = LoggerFactory.getLogger(PermissionCheckAspect.class);

    @Autowired
    private PermissionService permissionService;

    /**
     * 权限检查切入点
     * 拦截所有标注了 @RequirePermission 的方法
     */
    @Before("@annotation(requirePermission)")
    public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
        // 检查是否已禁用权限检查
        if (!requirePermission.enabled()) {
            logger.debug("权限检查已禁用");
            return;
        }

        try {
            // 获取权限代码
            String permissionCode = requirePermission.value();
            logger.debug("检查权限: {}", permissionCode);

            // 获取用户ID
            BigInteger userId = getUserId(joinPoint, requirePermission);
            if (userId == null) {
                logger.warn("无法获取用户ID，权限检查跳过");
                return;
            }

            // 获取 Topic ID（如果需要）
            BigInteger topicId = null;
            String topicIdParamName = requirePermission.topicIdParamName();
            if (topicIdParamName != null && !topicIdParamName.isEmpty()) {
                topicId = getTopicId(joinPoint, topicIdParamName);
            }

            // 执行权限检查
            try {
                permissionService.checkPermission(userId, topicId, permissionCode);
                logger.debug("权限检查通过: userId={}, topicId={}, permissionCode={}",
                        userId, topicId, permissionCode);
            } catch (PermissionService.PermissionDeniedException e) {
                // 记录日志
                if (requirePermission.logOnDeny()) {
                    logger.warn("权限检查失败: {}", e.getMessage());
                }

                // 使用自定义错误消息或默认消息
                String errorMessage = requirePermission.errorMessage();
                if (errorMessage == null || errorMessage.isEmpty()) {
                    errorMessage = e.getMessage();
                }

                // 抛出异常
                throw new PermissionService.PermissionDeniedException(userId, topicId, permissionCode);
            }

        } catch (PermissionService.PermissionDeniedException e) {
            // 重新抛出权限异常
            throw e;
        } catch (Exception e) {
            logger.error("权限检查执行异常", e);
            throw new RuntimeException("权限检查失败", e);
        }
    }

    /**
     * 获取用户ID
     * 优先级：
     * 1. 从方法参数中获取（如果 userIdParamName 已指定）
     * 2. 从请求头中获取（自定义 Header）
     */
    private BigInteger getUserId(JoinPoint joinPoint, RequirePermission requirePermission) {
        String userIdParamName = requirePermission.userIdParamName();

        // 方式1：从方法参数中获取
        if (userIdParamName != null && !userIdParamName.isEmpty()) {
            BigInteger userId = getParameterValue(joinPoint, userIdParamName, BigInteger.class);
            if (userId != null) {
                return userId;
            }
        }

        // 方式2：从请求头中获取
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String userIdHeader = request.getHeader("X-User-Id");
                if (userIdHeader != null && !userIdHeader.isEmpty()) {
                    return new BigInteger(userIdHeader);
                }
            }
        } catch (Exception e) {
            logger.debug("从请求头获取用户ID失败", e);
        }

        logger.warn("无法获取用户ID");
        return null;
    }

    /**
     * 获取 Topic ID
     * 从方法参数中获取
     */
    private BigInteger getTopicId(JoinPoint joinPoint, String topicIdParamName) {
        return getParameterValue(joinPoint, topicIdParamName, BigInteger.class);
    }

    /**
     * 从方法参数中获取指定名称的参数值
     *
     * @param joinPoint 连接点
     * @param paramName 参数名称
     * @param paramType 参数类型
     * @return 参数值（如果不存在或类型不匹配，返回 null）
     */
    private <T> T getParameterValue(JoinPoint joinPoint, String paramName, Class<T> paramType) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();

            // 获取参数名称和值
            String[] parameterNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            for (int i = 0; i < parameterNames.length; i++) {
                if (parameterNames[i].equals(paramName)) {
                    Object value = args[i];
                    if (paramType.isInstance(value)) {
                        return (T) value;
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("获取参数值失败: {}", paramName, e);
        }

        return null;
    }

    /**
     * 用户信息接口（可选）
     * 如果使用自定义 UserDetails，可以实现此接口
     */
    public interface UserInfo {
        Long getUserId();

        String getUsername();
    }
}
