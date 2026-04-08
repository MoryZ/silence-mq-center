package com.old.silence.mq.center.dto;

import org.apache.rocketmq.common.message.MessageQueue;

public class QueueOffsetInfo {

    private int idx;
    /** queue 时间范围内最小 offset */
    private long start;
    /** queue 时间范围内最大 offset */
    private long end;
    /** 当前页起始 offset */
    private long startOffset;
    /** 当前页结束 offset */
    private long endOffset;
    private MessageQueue messageQueues;

    public QueueOffsetInfo(int idx, long start, long end, long startOffset, long endOffset, MessageQueue messageQueues) {
        this.idx = idx;
        this.start = start;
        this.end = end;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.messageQueues = messageQueues;
    }

    public void incStartOffset() {
        startOffset++;
    }

    public void incStartOffset(long amount) {
        startOffset += amount;
    }

    public void incEndOffset() {
        endOffset++;
    }

    public int getIdx() { return idx; }

    public long getStart() { return start; }
    public void setStart(long start) { this.start = start; }

    public long getEnd() { return end; }
    public void setEnd(long end) { this.end = end; }

    public long getStartOffset() { return startOffset; }
    public void setStartOffset(long startOffset) { this.startOffset = startOffset; }

    public long getEndOffset() { return endOffset; }
    public void setEndOffset(long endOffset) { this.endOffset = endOffset; }

    public MessageQueue getMessageQueues() { return messageQueues; }
}
