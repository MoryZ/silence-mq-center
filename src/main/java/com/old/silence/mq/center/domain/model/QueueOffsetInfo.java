
package com.old.silence.mq.center.domain.model;

import org.apache.rocketmq.common.message.MessageQueue;

public class QueueOffsetInfo {
    private Integer idx;

    private Long start;
    private Long end;

    private Long startOffset;
    private Long endOffset;
    private MessageQueue messageQueues;

    public QueueOffsetInfo() {
    }

    public QueueOffsetInfo(Integer idx, Long start, Long end, Long startOffset, Long endOffset, MessageQueue messageQueues) {
        this.idx = idx;
        this.start = start;
        this.end = end;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.messageQueues = messageQueues;
    }

    public Integer getIdx() {
        return idx;
    }

    public void setIdx(Integer idx) {
        this.idx = idx;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public Long getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(Long startOffset) {
        this.startOffset = startOffset;
    }

    public Long getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(Long endOffset) {
        this.endOffset = endOffset;
    }

    public MessageQueue getMessageQueues() {
        return messageQueues;
    }

    public void setMessageQueues(MessageQueue messageQueues) {
        this.messageQueues = messageQueues;
    }

    public void incStartOffset() {
        this.startOffset++;
        this.endOffset++;
    }

    public void incEndOffset() {
        this.endOffset++;
    }

    public void incStartOffset(long size) {
        this.startOffset += size;
        this.endOffset += size;
    }
}
