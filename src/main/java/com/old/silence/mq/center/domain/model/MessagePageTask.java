package com.old.silence.mq.center.domain.model;

import org.springframework.data.domain.Page;

import java.util.List;

public class MessagePageTask {
    private Page<MessageView> page;
    private List<QueueOffsetInfo> queueOffsetInfos;

    public MessagePageTask(Page<MessageView> page, List<QueueOffsetInfo> queueOffsetInfos) {
        this.page = page;
        this.queueOffsetInfos = queueOffsetInfos;
    }

    public Page<MessageView> getPage() {
        return page;
    }

    public void setPage(Page<MessageView> page) {
        this.page = page;
    }

    public List<QueueOffsetInfo> getQueueOffsetInfos() {
        return queueOffsetInfos;
    }

    public void setQueueOffsetInfos(List<QueueOffsetInfo> queueOffsetInfos) {
        this.queueOffsetInfos = queueOffsetInfos;
    }

    @Override
    public String toString() {
        return "MessagePageTask{" +
                "page=" + page +
                ", queueOffsetInfos=" + queueOffsetInfos +
                '}';
    }
}
