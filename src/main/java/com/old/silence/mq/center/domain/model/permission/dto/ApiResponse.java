package com.old.silence.mq.center.domain.model.permission.dto;

/**
 * API响应结果DTO
 */
public class ApiResponse {

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 返回码
     */
    private Integer code;

    /**
     * 返回信息
     */
    private String message;

    /**
     * 返回数据
     */
    private Object data;

    /**
     * 时间戳
     */
    private Long timestamp;

    public ApiResponse() {
    }

    public ApiResponse(Boolean success, Integer code, String message, Object data, Long timestamp) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ApiResponse success(Object data) {
        return ApiResponse.builder()
                .success(true)
                .code(200)
                .message("success")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static ApiResponse error(Integer code, String message) {
        return ApiResponse.builder()
                .success(false)
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static ApiResponse error(String message) {
        return error(500, message);
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public static class Builder {
        private Boolean success;
        private Integer code;
        private String message;
        private Object data;
        private Long timestamp;

        public Builder success(Boolean success) {
            this.success = success;
            return this;
        }

        public Builder code(Integer code) {
            this.code = code;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder data(Object data) {
            this.data = data;
            return this;
        }

        public Builder timestamp(Long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ApiResponse build() {
            return new ApiResponse(success, code, message, data, timestamp);
        }
    }
}
