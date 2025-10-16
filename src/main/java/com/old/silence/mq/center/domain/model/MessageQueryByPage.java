
package com.old.silence.mq.center.domain.model;


import org.springframework.data.domain.PageRequest;

public class MessageQueryByPage {
    public static final int DEFAULT_PAGE = 0;

    public static final int MIN_PAGE_SIZE = 10;

    public static final int MAX_PAGE_SIZE = 100;

    /**
     * current page num
     */
    private int pageNo;

    private int pageSize;

    private String topic;
    private long begin;
    private long end;

    public MessageQueryByPage(int pageNo, int pageSize, String topic, long begin, long end) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.topic = topic;
        this.begin = begin;
        this.end = end;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public long getBegin() {
        return begin;
    }

    public void setBegin(long begin) {
        this.begin = begin;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public int getPageNo() {
        return pageNo <= 0 ? DEFAULT_PAGE : pageNo - 1;
    }

    public int getPageSize() {
        if (pageSize <= 1) {
            return MIN_PAGE_SIZE;
        } else if (pageSize > MAX_PAGE_SIZE) {
            return MAX_PAGE_SIZE;
        }
        return this.pageSize;
    }

    public PageRequest page() {
        return PageRequest.of(this.getPageNo(), this.getPageSize());
    }

    @Override
    public String toString() {
        return "MessageQueryByPage{" +
                "pageNo=" + pageNo +
                ", pageSize=" + pageSize +
                ", topic='" + topic + '\'' +
                ", begin=" + begin +
                ", end=" + end +
                '}';
    }
}
