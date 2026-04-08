package com.old.silence.mq.center.api.config;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({MQClientException.class, MQBrokerException.class})
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public Map<String, Object> handleMQException(Exception e) {
        logger.warn("MQ operation failed: {}", e.getMessage());
        return Map.of("code", 502, "message", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleException(Exception e) {
        logger.error("Unexpected error", e);
        return Map.of("code", 500, "message", e.getMessage());
    }
}
