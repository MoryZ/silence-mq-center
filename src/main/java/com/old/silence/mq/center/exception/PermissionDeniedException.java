package com.old.silence.mq.center.exception;


/**
 * 权限被拒绝异常
 *
 * @author moryzang
 */
public class PermissionDeniedException extends RuntimeException {
    public PermissionDeniedException(String message) {
        super(message);
    }

    public PermissionDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
