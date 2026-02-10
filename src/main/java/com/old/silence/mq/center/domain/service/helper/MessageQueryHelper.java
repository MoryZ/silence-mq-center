package com.old.silence.mq.center.domain.service.helper;

import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.consumer.PullStatus;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.old.silence.mq.center.domain.model.MessageQueryByPage;
import com.old.silence.mq.center.domain.model.QueueOffsetInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息查询辅助类 - 处理复杂的offset计算和消息拉取逻辑
 * 职责：
 * 1. 初始化queue offset信息
 * 2. 根据时间范围调整offset
 * 3. 处理分页offset的移动逻辑
 * 4. 简化消息拉取迭代过程
 */
public class MessageQueryHelper {

    private static final Logger logger = LoggerFactory.getLogger(MessageQueryHelper.class);

    /**
     * 初始化Queue Offset信息
     * 根据查询时间范围获取每个队列的起始和结束offset
     */
    public static List<QueueOffsetInfo> initializeQueueOffsets(
            DefaultMQPullConsumer consumer,
            MessageQueryByPage query) throws Exception {

        List<QueueOffsetInfo> queueOffsetInfos = new ArrayList<>();
        int idx = 0;

        for (MessageQueue mq : consumer.fetchSubscribeMessageQueues(query.getTopic())) {
            long minOffset = consumer.searchOffset(mq, query.getBegin());
            long maxOffset = consumer.searchOffset(mq, query.getEnd());
            queueOffsetInfos.add(new QueueOffsetInfo(idx++, minOffset, maxOffset, minOffset, minOffset, mq));
        }

        return queueOffsetInfos;
    }

    /**
     * 调整起始offset - 过滤掉时间范围外的消息
     * 向前移动offset直到找到第一条在时间范围内的消息
     */
    public static void adjustStartOffsets(
            DefaultMQPullConsumer consumer,
            List<QueueOffsetInfo> queueOffsets,
            MessageQueryByPage query) throws Exception {

        for (QueueOffsetInfo queueOffset : queueOffsets) {
            long start = queueOffset.getStart();
            boolean hasData = false;
            boolean hasIllegalOffset = true;

            while (hasIllegalOffset) {
                PullResult pullResult = consumer.pull(queueOffset.getMessageQueues(), "*", start, 32);

                if (pullResult.getPullStatus() == PullStatus.FOUND) {
                    hasData = true;
                    for (MessageExt msg : pullResult.getMsgFoundList()) {
                        if (msg.getStoreTimestamp() < query.getBegin()) {
                            start++;
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
            queueOffset.setStart(start);
            queueOffset.setStartOffset(start);
            queueOffset.setEndOffset(start);
        }
    }

    /**
     * 调整结束offset - 过滤掉时间范围外的消息
     * 向后移动offset直到找到最后一条在时间范围内的消息
     */
    public static void adjustEndOffsets(
            DefaultMQPullConsumer consumer,
            List<QueueOffsetInfo> queueOffsets,
            MessageQueryByPage query) throws Exception {

        for (QueueOffsetInfo queueOffset : queueOffsets) {
            if (queueOffset.getStart().equals(queueOffset.getEnd())) {
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
                    List<MessageExt> msgList = pullResult.getMsgFoundList();
                    for (int i = msgList.size() - 1; i >= 0; i--) {
                        if (msgList.get(i).getStoreTimestamp() > query.getEnd()) {
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
        }
    }

    /**
     * 计算所有队列的消息总数
     */
    public static long calculateTotalMessages(List<QueueOffsetInfo> queueOffsets) {
        return queueOffsets.stream()
                .mapToLong(q -> q.getEnd() - q.getStart())
                .sum();
    }

    /**
     * 移动起始offset用于分页
     * 根据页数和页大小计算起始offset的新位置
     */
    public static int moveStartOffset(List<QueueOffsetInfo> queueOffsets, MessageQueryByPage query) {
        int size = queueOffsets.size();
        long offset = (long) query.getPageNo() * query.getPageSize();

        if (offset == 0) {
            return 0;
        }

        // 按队列大小排序
        List<QueueOffsetInfo> sortedQueues = queueOffsets.stream()
                .sorted((o1, o2) -> {
                    long size1 = o1.getEnd() - o1.getStart();
                    long size2 = o2.getEnd() - o2.getStart();
                    return Long.compare(size1, size2);
                })
                .collect(Collectors.toList());

        // 平衡每个队列的offset
        for (int i = 0; i < size && offset >= (size - i); i++) {
            long minSize = sortedQueues.get(i).getEnd() - sortedQueues.get(i).getStartOffset();
            if (minSize == 0) {
                continue;
            }

            long reduce = minSize * (size - i);
            if (reduce <= offset) {
                offset -= reduce;
                for (int j = i; j < size; j++) {
                    sortedQueues.get(j).incStartOffset(minSize);
                }
            } else {
                long addOffset = offset / (size - i);
                offset -= addOffset * (size - i);
                if (addOffset != 0) {
                    for (int j = i; j < size; j++) {
                        sortedQueues.get(j).incStartOffset(addOffset);
                    }
                }
            }
        }

        // 同步回原始列表
        for (QueueOffsetInfo info : sortedQueues) {
            QueueOffsetInfo original = queueOffsets.get(info.getIdx());
            original.setStartOffset(info.getStartOffset());
            original.setEndOffset(info.getEndOffset());
        }

        // 处理剩余的offset
        int next = 0;
        for (QueueOffsetInfo info : queueOffsets) {
            if (offset == 0) break;
            next = (next + 1) % size;
            if (info.getStartOffset() < info.getEnd()) {
                info.incStartOffset();
                offset--;
            }
        }

        return next;
    }

    /**
     * 移动结束offset用于分页
     * 为当前页移动结束offset指向正确的消息范围
     */
    public static void moveEndOffset(List<QueueOffsetInfo> queueOffsets, MessageQueryByPage query, int startIdx) {
        int size = queueOffsets.size();
        int next = startIdx;

        for (int j = 0; j < query.getPageSize(); j++) {
            QueueOffsetInfo current = queueOffsets.get(next);
            next = (next + 1) % size;

            int start = next;
            while (current.getEndOffset() >= current.getEnd()) {
                current = queueOffsets.get(next);
                next = (next + 1) % size;

                if (start == next) {
                    return;
                }
            }

            current.incEndOffset();
        }
    }
}
