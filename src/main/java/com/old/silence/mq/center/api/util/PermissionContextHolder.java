package com.old.silence.mq.center.api.util;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigInteger;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.old.silence.mq.center.exception.ServiceException;

/**
 * 权限上下文管理器
 * <p>
 * 从HTTP请求中获取当前用户信息（ID和名称）
 * 提供统一的用户信息获取接口
 * 
 * @author Silence
 * @since 2024-01-01
 */
public class PermissionContextHolder {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_NAME_HEADER = "X-User-Name";
    private static final String UNKNOWN_USER = "UNKNOWN";

    /**
     * 获取当前请求中的用户ID
     *
     * @return 用户ID
     * @throws ServiceException 无法获取用户ID时抛出
     */
    public static BigInteger getCurrentUserId() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String userId = request.getHeader(USER_ID_HEADER);
                if (userId != null && !userId.isEmpty()) {
                    return new BigInteger(userId);
                }
            }
            throw new ServiceException(401, "无法获取当前用户信息，请在请求头中传入 " + USER_ID_HEADER);
        } catch (NumberFormatException e) {
            throw new ServiceException(400, "用户 ID 格式不正确");
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(500, "获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前请求中的用户名称
     *
     * @return 用户名称，获取失败返回"UNKNOWN"
     */
    public static String getCurrentUserName() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String userName = request.getHeader(USER_NAME_HEADER);
                if (userName != null && !userName.isEmpty()) {
                    return userName;
                }
            }
            return UNKNOWN_USER;
        } catch (Exception e) {
            return UNKNOWN_USER;
        }
    }
}
