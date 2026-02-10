package com.old.silence.mq.center.domain.service.template;

import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.consumer.PullStatus;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.old.silence.mq.center.domain.model.MessageView;
import com.old.silence.mq.center.domain.model.QueueOffsetInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息拉取和处理模板
 * 职责：
 * 1. 标准化消息拉取流程
 * 2. 处理拉取状态转换
 * 3. 支持自定义消息处理逻辑
 * 4. 简化消息转换过程
 */
public class MessagePullTemplate {

    private static final Logger logger = LoggerFactory.getLogger(MessagePullTemplate.class);

    /**
     * 从单个队列拉取消息
     *
     * @param consumer  消费者实例
     * @param queue     消息队列
     * @param offset    起始offset
     * @param pullSize  拉取数量
     * @param processor 消息处理器
     * @param <T>       处理结果类型
     */
    public static <T> void pullMessages(
            DefaultMQPullConsumer consumer,
            MessageQueue queue,
            long offset,
            int pullSize,
            MessageProcessor<T> processor) throws Exception {

        PullResult pullResult = consumer.pull(queue, "*", offset, pullSize);

        if (pullResult.getPullStatus() == PullStatus.FOUND) {
            for (MessageExt msg : pullResult.getMsgFoundList()) {
                processor.process(msg);
            }
        }
    }

    /**
     * 从单个队列拉取指定数量的消息
     *
     * @param consumer 消费者实例
     * @param queue    消息队列
     * @param offset   起始offset
     * @param count    需要拉取的消息数量
     * @return MessageView列表
     */
    public static List<MessageView> pullMessagesAsViews(
            DefaultMQPullConsumer consumer,
            MessageQueue queue,
            long offset,
            long count) throws Exception {

        List<MessageView> result = new ArrayList<>();
        long pulled = 0;

        while (pulled < count) {
            int batchSize = (int) Math.min(32, count - pulled);
            PullResult pullResult = consumer.pull(queue, "*", offset, batchSize);

            if (pullResult.getPullStatus() == PullStatus.FOUND) {
                List<MessageView> batch = pullResult.getMsgFoundList().stream()
                        .map(MessageView::fromMessageExt)
                        .collect(Collectors.toList());

                for (MessageView view : batch) {
                    if (pulled < count) {
                        result.add(view);
                        pulled++;
                    }
                }
                offset = pullResult.getNextBeginOffset();
            } else {
                break;
            }
        }

        return result;
    }

    /**
     * 拉取消息时过滤时间范围
     *
     * @param consumer  消费者实例
     * @param queue     消息队列
     * @param offset    起始offset
     * @param beginTime 开始时间戳
     * @param endTime   结束时间戳
     * @param maxCount  最大拉取数量
     * @return 过滤后的MessageView列表
     */
    public static List<MessageView> pullMessagesInTimeRange(
            DefaultMQPullConsumer consumer,
            MessageQueue queue,
            long offset,
            long beginTime,
            long endTime,
            int maxCount) throws Exception {

        List<MessageView> result = new ArrayList<>();

        while (result.size() < maxCount) {
            PullResult pullResult = consumer.pull(queue, "*", offset, 32);

            if (pullResult.getPullStatus() == PullStatus.FOUND) {
                for (MessageExt msg : pullResult.getMsgFoundList()) {
                    if (msg.getStoreTimestamp() >= beginTime && msg.getStoreTimestamp() <= endTime) {
                        MessageView view = MessageView.fromMessageExt(msg);
                        if (result.size() < maxCount) {
                            result.add(view);
                        }
                    }
                }
                offset = pullResult.getNextBeginOffset();
            } else {
                break;
            }
        }

        return result;
    }

    /**
     * 拉取指定offset范围的消息
     *
     * @param consumer     消费者实例
     * @param queueOffsets 队列offset信息列表
     * @param maxCount     最大拉取数量
     * @return MessageView列表
     */
    public static List<MessageView> pullMessagesFromQueues(
            DefaultMQPullConsumer consumer,
            List<QueueOffsetInfo> queueOffsets,
            long maxCount) throws Exception {

        List<MessageView> result = new ArrayList<>();
        long pulled = 0;

        for (QueueOffsetInfo queueInfo : queueOffsets) {
            long start = queueInfo.getStartOffset();
            long end = queueInfo.getEndOffset();
            long size = Math.min(end - start, maxCount - pulled);

            if (size == 0) {
                continue;
            }

            while (size > 0 && pulled < maxCount) {
                PullResult pullResult = consumer.pull(queueInfo.getMessageQueues(), "*", start, 32);

                if (pullResult.getPullStatus() == PullStatus.FOUND) {
                    for (MessageExt msg : pullResult.getMsgFoundList()) {
                        if (size > 0 && pulled < maxCount) {
                            result.add(MessageView.fromMessageExt(msg));
                            size--;
                            pulled++;
                        }
                    }
                } else {
                    break;
                }
            }

            if (pulled >= maxCount) {
                break;
            }
        }

        return result;
    }

    /**
     * 消息处理器接口
     */
    @FunctionalInterface
    public interface MessageProcessor<T> {
        void process(MessageExt message) throws Exception;
    }
}
