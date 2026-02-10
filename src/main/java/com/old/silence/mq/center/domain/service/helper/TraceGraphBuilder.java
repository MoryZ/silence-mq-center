package com.old.silence.mq.center.domain.service.helper;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.trace.TraceType;
import org.apache.rocketmq.common.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.old.silence.mq.center.domain.model.MessageTraceView;
import com.old.silence.mq.center.domain.model.trace.MessageTraceGraph;
import com.old.silence.mq.center.domain.model.trace.MessageTraceStatusEnum;
import com.old.silence.mq.center.domain.model.trace.ProducerNode;
import com.old.silence.mq.center.domain.model.trace.SubscriptionNode;
import com.old.silence.mq.center.domain.model.trace.TraceNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 消息追踪图构建辅助类
 * 职责：处理MessageTraceView列表到MessageTraceGraph的转换逻辑
 */
public class TraceGraphBuilder {

    private static final String UNKNOWN_GROUP_NAME = "%UNKNOWN_GROUP%";
    private static final int MESSAGE_TRACE_MISSING_VALUE = -1;

    /**
     * 从MessageTraceView列表构建追踪图
     */
    public static MessageTraceGraph buildGraph(List<MessageTraceView> messageTraceViews) {
        MessageTraceGraph graph = new MessageTraceGraph();
        graph.setMessageTraceViews(messageTraceViews);

        if (CollectionUtils.isEmpty(messageTraceViews)) {
            return graph;
        }

        // 解析生产者节点和事务节点
        ProducerNode producerNode = null;
        List<TraceNode> transactionNodeList = new ArrayList<>();
        Map<String, Pair<MessageTraceView, MessageTraceView>> requestIdTracePairMap = Maps.newHashMap();

        for (MessageTraceView traceView : messageTraceViews) {
            TraceType traceType = TraceType.valueOf(traceView.getTraceType());
            switch (traceType) {
                case Pub:
                    producerNode = buildProducerNode(traceView);
                    break;
                case EndTransaction:
                    transactionNodeList.add(buildTransactionNode(traceView));
                    break;
                case SubBefore:
                case SubAfter:
                    putTraceViewPair(traceView, requestIdTracePairMap);
                    break;
                default:
                    break;
            }
        }

        // 设置生产者信息和事务节点
        if (producerNode != null) {
            producerNode.setTransactionNodeList(sortByBeginTimestamp(transactionNodeList));
        }
        graph.setProducerNode(producerNode);

        // 构建订阅节点列表
        graph.setSubscriptionNodeList(buildSubscriptionNodeList(requestIdTracePairMap));

        return graph;
    }

    /**
     * 构建生产者根节点
     */
    private static ProducerNode buildProducerNode(MessageTraceView traceView) {
        ProducerNode root = new ProducerNode();
        BeanUtils.copyProperties(traceView, root);
        root.setTraceNode(buildTraceNode(traceView));
        return root;
    }

    /**
     * 构建事务节点
     */
    private static TraceNode buildTransactionNode(MessageTraceView traceView) {
        TraceNode node = buildTraceNode(traceView);
        node.setCostTime(MESSAGE_TRACE_MISSING_VALUE);
        return node;
    }

    /**
     * 从Pair构建消费消息追踪节点
     */
    private static TraceNode buildConsumeMessageTraceNode(Pair<MessageTraceView, MessageTraceView> pair) {
        MessageTraceView subBeforeTrace = pair.getObject1();
        MessageTraceView subAfterTrace = pair.getObject2();

        TraceNode consumeNode = new TraceNode();
        consumeNode.setRequestId(getTraceValue(pair, MessageTraceView::getRequestId));
        consumeNode.setStoreHost(getTraceValue(pair, MessageTraceView::getStoreHost));
        consumeNode.setClientHost(getTraceValue(pair, MessageTraceView::getClientHost));

        // 设置Before追踪信息
        if (subBeforeTrace != null) {
            consumeNode.setRetryTimes(subBeforeTrace.getRetryTimes());
            consumeNode.setBeginTimestamp(subBeforeTrace.getTimeStamp());
        } else {
            consumeNode.setRetryTimes(MESSAGE_TRACE_MISSING_VALUE);
            consumeNode.setBeginTimestamp(MESSAGE_TRACE_MISSING_VALUE);
        }

        // 设置After追踪信息
        if (subAfterTrace != null) {
            consumeNode.setCostTime(subAfterTrace.getCostTime());
            consumeNode.setStatus(subAfterTrace.getStatus());

            if (subAfterTrace.getTimeStamp() > 0) {
                consumeNode.setEndTimestamp(subAfterTrace.getTimeStamp());
            } else {
                // 计算结束时间
                if (subBeforeTrace != null) {
                    if (subAfterTrace.getCostTime() >= 0) {
                        consumeNode.setEndTimestamp(subBeforeTrace.getTimeStamp() + subAfterTrace.getCostTime());
                    } else {
                        consumeNode.setEndTimestamp(subBeforeTrace.getTimeStamp());
                    }
                } else {
                    consumeNode.setEndTimestamp(MESSAGE_TRACE_MISSING_VALUE);
                }
            }
        } else {
            consumeNode.setCostTime(MESSAGE_TRACE_MISSING_VALUE);
            consumeNode.setEndTimestamp(MESSAGE_TRACE_MISSING_VALUE);
            consumeNode.setStatus(MessageTraceStatusEnum.UNKNOWN.getStatus());
        }

        return consumeNode;
    }

    /**
     * 构建订阅节点列表
     */
    private static List<SubscriptionNode> buildSubscriptionNodeList(
            Map<String, Pair<MessageTraceView, MessageTraceView>> requestIdTracePairMap) {

        Map<String, List<TraceNode>> subscriptionTraceNodeMap = Maps.newHashMap();

        for (Pair<MessageTraceView, MessageTraceView> pair : requestIdTracePairMap.values()) {
            String groupName = buildGroupName(pair);
            List<TraceNode> traceNodeList = subscriptionTraceNodeMap
                    .computeIfAbsent(groupName, k -> new ArrayList<>());
            traceNodeList.add(buildConsumeMessageTraceNode(pair));
        }

        return subscriptionTraceNodeMap.entrySet().stream()
                .map(entry -> {
                    SubscriptionNode node = new SubscriptionNode();
                    node.setSubscriptionGroup(entry.getKey());
                    node.setConsumeNodeList(sortByBeginTimestamp(entry.getValue()));
                    return node;
                })
                .collect(Collectors.toList());
    }

    /**
     * 构建基础TraceNode
     */
    private static TraceNode buildTraceNode(MessageTraceView traceView) {
        TraceNode node = new TraceNode();
        BeanUtils.copyProperties(traceView, node);
        node.setBeginTimestamp(traceView.getTimeStamp());
        node.setEndTimestamp(traceView.getTimeStamp() + traceView.getCostTime());
        return node;
    }

    /**
     * 将TraceView存入Map，合并SubBefore和SubAfter
     */
    private static void putTraceViewPair(MessageTraceView traceView,
                                         Map<String, Pair<MessageTraceView, MessageTraceView>> map) {
        Pair<MessageTraceView, MessageTraceView> pair = map
                .computeIfAbsent(traceView.getRequestId(), k -> new Pair<>(null, null));

        TraceType traceType = TraceType.valueOf(traceView.getTraceType());
        if (traceType == TraceType.SubBefore) {
            pair.setObject1(traceView);
        } else if (traceType == TraceType.SubAfter) {
            pair.setObject2(traceView);
        }
    }

    /**
     * 从Pair中获取TraceView值
     */
    private static <E> E getTraceValue(Pair<MessageTraceView, MessageTraceView> pair,
                                       Function<MessageTraceView, E> function) {
        if (pair.getObject1() != null) {
            return function.apply(pair.getObject1());
        }
        return function.apply(pair.getObject2());
    }

    /**
     * 构建消费者组名称
     */
    private static String buildGroupName(Pair<MessageTraceView, MessageTraceView> pair) {
        String groupName = getTraceValue(pair, MessageTraceView::getGroupName);
        if (StringUtils.isNotBlank(groupName)) {
            return groupName;
        }
        return UNKNOWN_GROUP_NAME;
    }

    /**
     * 按开始时间戳排序TraceNode列表
     */
    private static List<TraceNode> sortByBeginTimestamp(List<TraceNode> traceNodeList) {
        traceNodeList.sort((o1, o2) -> -Long.compare(o1.getBeginTimestamp(), o2.getBeginTimestamp()));
        return traceNodeList;
    }
}
