package com.old.silence.mq.center.domain.model.dto;

import java.util.List;

/**
 * 消息查询结果 DTO
 */
public class MessageQueryResultDTO {

    /**
     * 消息列表
     */
    private List<MessageViewDTO> messages;

    /**
     * 总数
     */
    private long totalCount;

    /**
     * 当前页
     */
    private int currentPage;

    /**
     * 页大小
     */
    private int pageSize;

    /**
     * 总页数
     */
    private int totalPages;

    public MessageQueryResultDTO() {
    }

    public MessageQueryResultDTO(List<MessageViewDTO> messages, long totalCount, int currentPage, int pageSize,
                                 int totalPages) {
        this.messages = messages;
        this.totalCount = totalCount;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<MessageViewDTO> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageViewDTO> messages) {
        this.messages = messages;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public static class Builder {
        private List<MessageViewDTO> messages;
        private long totalCount;
        private int currentPage;
        private int pageSize;
        private int totalPages;

        public Builder messages(List<MessageViewDTO> messages) {
            this.messages = messages;
            return this;
        }

        public Builder totalCount(long totalCount) {
            this.totalCount = totalCount;
            return this;
        }

        public Builder currentPage(int currentPage) {
            this.currentPage = currentPage;
            return this;
        }

        public Builder pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder totalPages(int totalPages) {
            this.totalPages = totalPages;
            return this;
        }

        public MessageQueryResultDTO build() {
            return new MessageQueryResultDTO(messages, totalCount, currentPage, pageSize, totalPages);
        }
    }
}
