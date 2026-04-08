package com.old.silence.mq.center.domain.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.rocketmq.client.QueryResult;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.consumer.PullStatus;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.protocol.body.ConsumeMessageDirectlyResult;
import org.apache.rocketmq.tools.admin.api.MessageTrack;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.mq.center.api.config.RMQConfigure;
import com.old.silence.mq.center.dto.MessagePage;
import com.old.silence.mq.center.dto.MessageView;
import com.old.silence.mq.center.dto.MessageQuery;
import com.old.silence.mq.center.dto.QueueOffsetInfo;
import org.apache.commons.lang3.StringUtils;

/**
 * @author moryzang
 */
@Service
public class MessageService {

    private static final Cache<String, List<QueueOffsetInfo>> CACHE = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .build();

    /**
     * @see org.apache.rocketmq.store.config.MessageStoreConfig maxMsgsNumBatch = 64
     * @see org.apache.rocketmq.store.index.IndexService maxNum = Math.min(maxNum, ...)
     */
    private static final int QUERY_MESSAGE_MAX_NUM = 64;

    private final MQAdminService mqAdminService;
    private final RMQConfigure configure;

    public MessageService(MQAdminService mqAdminService, RMQConfigure configure) {
        this.mqAdminService = mqAdminService;
        this.configure = configure;
    }

    public Map<String, Object> viewMessage(String topic, String msgId) throws Exception {
        return mqAdminService.execute(admin -> {
            MessageExt messageExt = admin.viewMessage(topic, msgId);

            Map<String, Object> view = new HashMap<>();
            view.put("msgId", messageExt.getMsgId());
            view.put("topic", messageExt.getTopic());
            view.put("body", new String(messageExt.getBody(), StandardCharsets.UTF_8));
            view.put("tags", messageExt.getTags());
            view.put("keys", messageExt.getKeys());
            view.put("bornTimestamp", messageExt.getBornTimestamp());
            view.put("deliveryTimestamp", messageExt.getDeliverTimeMs());

            List<MessageTrack> tracks = admin.messageTrackDetail(messageExt);
            Map<String, Object> result = new HashMap<>();
            result.put("messageView", view);
            result.put("messageTrackList", tracks);
            return result;
        });
    }

    public MessagePage queryMessageByPage(MessageQuery query) {
        if (query == null || StringUtils.isBlank(query.getTopic())) {
            return new MessagePage(new Page<>(), null);
        }

        // 时间窗口兜底：
        // 1) 不传 end => 默认当前时间
        // 2) 不传 begin => 默认最近24小时
        // 3) begin/end 传秒级时间戳时自动转毫秒
        long now = System.currentTimeMillis();
        long end = normalizeTimestamp(query.getEnd());
        if (end <= 0) {
            end = now;
        }

        long begin = normalizeTimestamp(query.getBegin());
        if (begin <= 0) {
            begin = end - TimeUnit.HOURS.toMillis(24);
        }

        if (begin > end) {
            long tmp = begin;
            begin = end;
            end = tmp;
        }

        query.setBegin(begin);
        query.setEnd(end);

        if (query.getPageNo() <= 0) {
            query.setPageNo(1);
        }
        if (query.getPageSize() <= 0) {
            query.setPageSize(20);
        }

        String taskId = query.getTaskId();
        if (StringUtils.isBlank(taskId) && query.getPageNo() > 1) {
            throw new IllegalArgumentException("taskId is required when pageNo > 1");
        }

        List<QueueOffsetInfo> queueOffsetInfos = StringUtils.isBlank(taskId) ? null : CACHE.getIfPresent(taskId);

        if (StringUtils.isNotBlank(taskId) && queueOffsetInfos == null) {
            throw new IllegalArgumentException("taskId is invalid or expired, please query pageNo=1 without taskId to refresh");
        }

        if (queueOffsetInfos == null) {
            // 首次请求：遍历各 Queue 建立偏移量缓存
            MessagePageTask task = queryFirstMessagePage(query);
            String newTaskId = UUID.randomUUID().toString();
            CACHE.put(newTaskId, task.queueOffsetInfos());
            return new MessagePage(task.page(), newTaskId);
        }

        // 后续翻页：直接用缓存偏移量切片
        Page<MessageView> page = queryMessageByTaskPage(query, queueOffsetInfos);
        return new MessagePage(page, taskId);
    }

    private long normalizeTimestamp(long ts) {
        if (ts <= 0) {
            return ts;
        }
        // 10位时间戳按秒处理（例如 1775044810）
        if (ts < 1_000_000_000_000L) {
            return ts * 1000;
        }
        return ts;
    }

    public List<MessageView> queryMessageByTopicAndKey(String topic, String key) throws Exception {
        if (StringUtils.isBlank(topic) || StringUtils.isBlank(key)) {
            return new ArrayList<>();
        }
        return mqAdminService.execute(admin -> {
            QueryResult queryResult = admin.queryMessage(topic, key, QUERY_MESSAGE_MAX_NUM, 0, System.currentTimeMillis());
            List<MessageView> messageViews = new ArrayList<>();
            if (queryResult != null && queryResult.getMessageList() != null) {
                for (MessageExt msg : queryResult.getMessageList()) {
                    messageViews.add(MessageView.fromMessageExt(msg));
                }
            }
            return messageViews;
        });
    }

    @SuppressWarnings("deprecation")
    public List<MessageView> queryMessageByTopic(String topic, long begin, long end) throws Exception {
        if (StringUtils.isBlank(topic)) {
            return new ArrayList<>();
        }
        long start = begin > 0 ? begin : 0;
        long stop = end > 0 ? end : System.currentTimeMillis();

        DefaultMQPullConsumer consumer = buildDefaultMQPullConsumer();
        List<MessageView> messageViews = new ArrayList<>();
        try {
            consumer.start();
            Set<MessageQueue> queues = consumer.fetchSubscribeMessageQueues(topic);
            for (MessageQueue mq : queues) {
                long minOffset = consumer.searchOffset(mq, start);
                long maxOffset = consumer.searchOffset(mq, stop);
                long offset = minOffset;

                while (offset < maxOffset && messageViews.size() < 2000) {
                    PullResult pullResult = consumer.pull(mq, "*", offset, 32);
                    offset = pullResult.getNextBeginOffset();
                    if (pullResult.getPullStatus() == PullStatus.FOUND && pullResult.getMsgFoundList() != null) {
                        for (MessageExt msg : pullResult.getMsgFoundList()) {
                            long ts = msg.getStoreTimestamp();
                            if (ts >= start && ts <= stop) {
                                messageViews.add(MessageView.fromMessageExt(msg));
                            }
                        }
                    }

                    if (pullResult.getPullStatus() == PullStatus.NO_NEW_MSG
                            || pullResult.getPullStatus() == PullStatus.NO_MATCHED_MSG
                            || pullResult.getPullStatus() == PullStatus.OFFSET_ILLEGAL) {
                        break;
                    }
                }
            }

            messageViews.sort(Comparator.comparingLong(MessageView::getStoreTimestamp).reversed());
            return messageViews;
        } finally {
            consumer.shutdown();
        }
    }

    public ConsumeMessageDirectlyResult consumeMessageDirectly(String topic, String msgId, String consumerGroup, String clientId) throws Exception {
        if (StringUtils.isBlank(topic) || StringUtils.isBlank(msgId) || StringUtils.isBlank(consumerGroup)) {
            return null;
        }
        return mqAdminService.execute(admin -> admin.consumeMessageDirectly(consumerGroup, topic, msgId, clientId));
    }

    // ---------- private ----------

    private record MessagePageTask(Page<MessageView> page, List<QueueOffsetInfo> queueOffsetInfos) {}

    @SuppressWarnings("deprecation")
    private MessagePageTask queryFirstMessagePage(MessageQuery query) {
        DefaultMQPullConsumer consumer = buildDefaultMQPullConsumer();
        long total = 0;
        List<QueueOffsetInfo> queueOffsetInfos = new ArrayList<>();
        List<MessageView> messageViews = new ArrayList<>();

        try {
            consumer.start();
            Set<MessageQueue> messageQueues = consumer.fetchSubscribeMessageQueues(query.getTopic());
            int idx = 0;
            for (MessageQueue mq : messageQueues) {
                long minOffset = consumer.searchOffset(mq, query.getBegin());
                long maxOffset = consumer.searchOffset(mq, query.getEnd());
                queueOffsetInfos.add(new QueueOffsetInfo(idx++, minOffset, maxOffset, minOffset, minOffset, mq));
            }

            // 修剪 begin 边界：跳过 storeTimestamp < begin 的消息
            for (QueueOffsetInfo queueOffset : queueOffsetInfos) {
                long offset = queueOffset.getStart();
                boolean hasData = false;
                boolean hasIllegalOffset = true;
                while (hasIllegalOffset) {
                    PullResult pullResult = consumer.pull(queueOffset.getMessageQueues(), "*", offset, 32);
                    if (pullResult.getPullStatus() == PullStatus.FOUND) {
                        hasData = true;
                        for (MessageExt msg : pullResult.getMsgFoundList()) {
                            if (msg.getStoreTimestamp() < query.getBegin()) {
                                offset++;
                            } else {
                                hasIllegalOffset = false;
                                break;
                            }
                        }
                    } else {
                        hasIllegalOffset = false;
                    }
                }
                if (!hasData) {
                    queueOffset.setEnd(queueOffset.getStart());
                }
                queueOffset.setStart(offset);
                queueOffset.setStartOffset(offset);
                queueOffset.setEndOffset(offset);
            }

            // 修剪 end 边界：跳过 storeTimestamp > end 的消息
            for (QueueOffsetInfo queueOffset : queueOffsetInfos) {
                if (queueOffset.getStart() == queueOffset.getEnd()) {
                    continue;
                }
                long end = queueOffset.getEnd();
                long pullOffset = end;
                int pullSize = 32;
                boolean hasIllegalOffset = true;
                while (hasIllegalOffset) {
                    if (pullOffset - pullSize > queueOffset.getStart()) {
                        pullOffset = pullOffset - pullSize;
                    } else {
                        pullOffset = queueOffset.getStartOffset();
                        pullSize = (int) (end - pullOffset);
                    }
                    PullResult pullResult = consumer.pull(queueOffset.getMessageQueues(), "*", pullOffset, pullSize);
                    if (pullResult.getPullStatus() == PullStatus.FOUND) {
                        List<MessageExt> found = pullResult.getMsgFoundList();
                        for (int i = found.size() - 1; i >= 0; i--) {
                            if (found.get(i).getStoreTimestamp() > query.getEnd()) {
                                end--;
                            } else {
                                hasIllegalOffset = false;
                                break;
                            }
                        }
                    } else {
                        hasIllegalOffset = false;
                    }
                    if (pullOffset == queueOffset.getStartOffset()) {
                        break;
                    }
                }
                queueOffset.setEnd(end);
                total += queueOffset.getEnd() - queueOffset.getStart();
            }

            long pageSize = Math.min(total, query.getPageSize());
            int next = moveStartOffset(queueOffsetInfos, query);
            moveEndOffset(queueOffsetInfos, query, next);

            for (QueueOffsetInfo info : queueOffsetInfos) {
                long start = info.getStartOffset();
                long end = info.getEndOffset();
                long size = Math.min(end - start, pageSize);
                if (size == 0) continue;
                while (size > 0) {
                    PullResult pullResult = consumer.pull(info.getMessageQueues(), "*", start, 32);
                    if (pullResult.getPullStatus() == PullStatus.FOUND) {
                        List<MessageExt> poll = pullResult.getMsgFoundList();
                        if (poll.isEmpty()) break;
                        for (MessageExt msg : poll) {
                            if (size > 0) {
                                messageViews.add(MessageView.fromMessageExt(msg));
                                size--;
                            }
                        }
                        start = pullResult.getNextBeginOffset();
                    } else {
                        break;
                    }
                }
            }

            Page<MessageView> page = new Page<>(query.getPageNo(), query.getPageSize(), total);
            page.setRecords(messageViews);
            return new MessagePageTask(page, queueOffsetInfos);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            consumer.shutdown();
        }
    }

    @SuppressWarnings("deprecation")
    private Page<MessageView> queryMessageByTaskPage(MessageQuery query, List<QueueOffsetInfo> queueOffsetInfos) {
        DefaultMQPullConsumer consumer = buildDefaultMQPullConsumer();
        List<MessageView> messageViews = new ArrayList<>();
        long total = 0;

        try {
            consumer.start();
            for (QueueOffsetInfo info : queueOffsetInfos) {
                info.setStartOffset(info.getStart());
                info.setEndOffset(info.getStart());
                total += info.getEnd() - info.getStart();
            }

            long offset = (long) (query.getPageNo() - 1) * query.getPageSize();
            if (total <= offset) {
                return new Page<>(query.getPageNo(), query.getPageSize(), total);
            }

            int next = moveStartOffset(queueOffsetInfos, query);
            moveEndOffset(queueOffsetInfos, query, next);

            long pageSize = Math.min(total - offset, query.getPageSize());
            for (QueueOffsetInfo info : queueOffsetInfos) {
                long start = info.getStartOffset();
                long end = info.getEndOffset();
                long size = Math.min(end - start, pageSize);
                if (size == 0) continue;
                while (size > 0) {
                    PullResult pullResult = consumer.pull(info.getMessageQueues(), "*", start, 32);
                    if (pullResult.getPullStatus() == PullStatus.FOUND) {
                        List<MessageExt> poll = pullResult.getMsgFoundList();
                        if (poll.isEmpty()) break;
                        for (MessageExt msg : poll) {
                            if (size > 0) {
                                messageViews.add(MessageView.fromMessageExt(msg));
                                size--;
                            }
                        }
                        start = pullResult.getNextBeginOffset();
                    } else {
                        break;
                    }
                }
            }

            Page<MessageView> page = new Page<>(query.getPageNo(), query.getPageSize(), total);
            page.setRecords(messageViews);
            return page;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            consumer.shutdown();
        }
    }

    private int moveStartOffset(List<QueueOffsetInfo> queueOffsets, MessageQuery query) {
        int size = queueOffsets.size();
        int next = 0;
        long offset = (long) (query.getPageNo() - 1) * query.getPageSize();
        if (offset == 0) return next;

        List<QueueOffsetInfo> orderQueue = queueOffsets.stream()
                .sorted((o1, o2) -> Long.compare(
                        o1.getEnd() - o1.getStart(),
                        o2.getEnd() - o2.getStart()))
                .collect(Collectors.toList());

        for (int i = 0; i < size && offset >= (size - i); i++) {
            long minSize = orderQueue.get(i).getEnd() - orderQueue.get(i).getStartOffset();
            if (minSize == 0) continue;
            long reduce = minSize * (size - i);
            if (reduce <= offset) {
                offset -= reduce;
                for (int j = i; j < size; j++) {
                    orderQueue.get(j).incStartOffset(minSize);
                }
            } else {
                long addOffset = offset / (size - i);
                offset -= addOffset * (size - i);
                if (addOffset != 0) {
                    for (int j = i; j < size; j++) {
                        orderQueue.get(j).incStartOffset(addOffset);
                    }
                }
            }
        }

        for (QueueOffsetInfo info : orderQueue) {
            QueueOffsetInfo target = queueOffsets.get(info.getIdx());
            target.setStartOffset(info.getStartOffset());
            target.setEndOffset(info.getEndOffset());
        }

        for (QueueOffsetInfo info : queueOffsets) {
            if (offset == 0) break;
            next = (next + 1) % size;
            if (info.getStartOffset() < info.getEnd()) {
                info.incStartOffset();
                --offset;
            }
        }
        return next;
    }

    private void moveEndOffset(List<QueueOffsetInfo> queueOffsets, MessageQuery query, int next) {
        int size = queueOffsets.size();
        for (int j = 0; j < query.getPageSize(); j++) {
            QueueOffsetInfo nextQueue = queueOffsets.get(next);
            next = (next + 1) % size;
            int start = next;
            while (nextQueue.getEndOffset() >= nextQueue.getEnd()) {
                nextQueue = queueOffsets.get(next);
                next = (next + 1) % size;
                if (start == next) return;
            }
            nextQueue.incEndOffset();
        }
    }

    @SuppressWarnings("deprecation")
    private DefaultMQPullConsumer buildDefaultMQPullConsumer() {
        DefaultMQPullConsumer consumer = new DefaultMQPullConsumer(MixAll.TOOLS_CONSUMER_GROUP);
        consumer.setNamesrvAddr(configure.getNamesrvAddr());
        consumer.setUseTLS(configure.isUseTLS());
        return consumer;
    }
}

