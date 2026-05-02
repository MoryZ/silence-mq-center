package com.old.silence.mq.center.domain.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.QueryResult;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Service;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.mq.center.dto.MessageTraceGraph;
import com.old.silence.mq.center.dto.ProducerNode;
import com.old.silence.mq.center.dto.SubscriptionNode;
import com.old.silence.mq.center.dto.TraceNode;
import com.old.silence.mq.center.vo.MessageTraceView;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author moryzang
 */
@Service
public class MessageTraceService {

    private static final int QUERY_MESSAGE_MAX_NUM = 64;
    private static final int QUERY_MESSAGE_EMPTY_CODE = 208;
    private static final String DEFAULT_TRACE_TOPIC = "RMQ_SYS_TRACE_TOPIC";

    private final MQAdminService mqAdminService;

    public MessageTraceService(MQAdminService mqAdminService) {
        this.mqAdminService = mqAdminService;
    }

    private int safeParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    private TraceNode buildTraceNode(MessageTraceView view) {
        TraceNode node = new TraceNode();
        node.setRequestId(view.getMsgId());
        node.setStoreHost(view.getStoreHost());
        node.setClientHost(view.getAddr());
        node.setCostTime(view.getCostTime());
        node.setRetryTimes(view.getRetryTimes());
        node.setStatus(view.isSuccess() ? "SUCCESS" : "FAILED");
        node.setBeginTimestamp(view.getTime());
        node.setEndTimestamp(view.getTime() + view.getCostTime());
        node.setMsgType(view.getTraceType());
        return node;
    }

    public List<MessageTraceView> queryMessageTraceKey(String msgId, String traceTopic) throws Exception {
        return mqAdminService.execute(admin -> {
            long end = System.currentTimeMillis();
            long start = end - (3L * 24 * 3600 * 1000);

            List<String> candidateTopics = new ArrayList<>();
            if (StringUtils.isNotBlank(traceTopic)) {
                candidateTopics.add(traceTopic);
            }
            if (candidateTopics.stream().noneMatch(DEFAULT_TRACE_TOPIC::equals)) {
                candidateTopics.add(DEFAULT_TRACE_TOPIC);
            }

            for (String currentTopic : candidateTopics) {
                List<MessageTraceView> traceViews = queryByTopic(admin, msgId, currentTopic, start, end);
                if (!traceViews.isEmpty()) {
                    return traceViews.stream()
                            .sorted(Comparator.comparingLong(MessageTraceView::getTime))
                            .collect(Collectors.toList());
                }
            }

            return new ArrayList<>();
        });
    }

    private List<MessageTraceView> queryByTopic(org.apache.rocketmq.tools.admin.DefaultMQAdminExt admin,
                                                String msgId,
                                                String traceTopic,
                                                long start,
                                                long end) throws Exception {
        List<MessageTraceView> traceViews = new ArrayList<>();

            QueryResult queryResult;
            try {
                queryResult = admin.queryMessage(traceTopic, msgId, QUERY_MESSAGE_MAX_NUM, start, end);
            } catch (MQClientException e) {
                // CODE 208: query message by key finished, but no message.
                if (e.getResponseCode() == QUERY_MESSAGE_EMPTY_CODE) {
                    return traceViews;
                }
                throw e;
            }

        if (queryResult == null || queryResult.getMessageList() == null) return traceViews;

        for (MessageExt traceMsg : queryResult.getMessageList()) {
            // 5.x 轨迹 Body 通常是多行字符串，每行代表一个生命周期阶段
            String body = new String(traceMsg.getBody(), StandardCharsets.UTF_8);
            String[] lines = body.split("\n"); // 轨迹数据按换行符分隔

            for (String line : lines) {
                if (StringUtils.isBlank(line)) continue;
                // 轨迹字段通常是以 \u0001 (ASCII 1) 分隔的
                String[] fields = line.split(String.valueOf((char) 1));

                // 只要这行数据包含我们的 msgId，就开始解析
                if (line.contains(msgId)) {
                    MessageTraceView view = new MessageTraceView();
                    // 根据 RocketMQ 轨迹协议解析字段 (简版逻辑)
                    // fields[0]: TraceType (Pub/Sub/EndTransaction)
                    // fields[1]: TimeStamp
                    // fields[2]: RegionId
                    // fields[3]: GroupName
                    view.setTraceType(fields[0]);
                    view.setTime(fields.length > 1 ? Long.parseLong(fields[1]) : 0L);
                    view.setRegionId(fields.length > 2 ? fields[2] : null);
                    view.setGroupName(fields.length > 3 ? fields[3] : null);
                    view.setMsgId(msgId);
                    view.setTopic(fields.length > 4 ? fields[4] : null);
                    view.setAddr(fields.length > 5 ? fields[5] : null);
                    view.setStoreHost(fields.length > 6 ? fields[6] : null);
                    view.setCostTime(fields.length > 7 ? safeParseInt(fields[7]) : 0);
                    view.setRetryTimes(fields.length > 8 ? safeParseInt(fields[8]) : 0);
                    view.setSuccess(true);

                    traceViews.add(view);
                }
            }
        }

        return traceViews;
    }

    public MessageTraceGraph queryMessageTraceGraph(String msgId, String traceTopic) throws Exception {
        // 1. 处理 Topic 优先级
        String finalTraceTopic = StringUtils.isNotBlank(traceTopic) ? traceTopic : DEFAULT_TRACE_TOPIC;

        // 2. 获取线性轨迹列表 (这一步我们之前已经实现了)
        List<MessageTraceView> details = this.queryMessageTraceKey(msgId, finalTraceTopic);

        MessageTraceGraph graph = new MessageTraceGraph();
        graph.setMessageTraceViews(details); // 填充原始流水

        if (CollectionUtils.isEmpty(details)) {
            return graph;
        }

        // 3. 组装 ProducerNode (取第一个 Pub 类型的记录)
        details.stream()
                .filter(t -> "Pub".equalsIgnoreCase(t.getTraceType()))
                .findFirst()
                .ifPresent(pub -> {
                    ProducerNode pNode = new ProducerNode();
                    pNode.setMsgId(pub.getMsgId());
                    pNode.setTopic(pub.getTopic());
                    pNode.setGroupName(pub.getGroupName());
                    pNode.setTraceNode(buildTraceNode(pub));

                    List<TraceNode> txnNodes = details.stream()
                            .filter(t -> "EndTransaction".equalsIgnoreCase(t.getTraceType()))
                            .map(this::buildTraceNode)
                            .collect(Collectors.toList());
                    pNode.setTransactionNodeList(txnNodes);

                    graph.setProducerNode(pNode);
                });

        // 4. 组装 SubscriptionNodeList (按 GroupName 分组)
        Map<String, List<MessageTraceView>> subMap = details.stream()
                .filter(t -> "Sub".equalsIgnoreCase(t.getTraceType()))
                .collect(Collectors.groupingBy(MessageTraceView::getGroupName));

        List<SubscriptionNode> subNodes = new ArrayList<>();
        subMap.forEach((groupName, traces) -> {
            SubscriptionNode sNode = new SubscriptionNode();
            sNode.setSubscriptionGroup(groupName);

            List<TraceNode> consumeNodes = traces.stream()
                    .map(this::buildTraceNode)
                    .collect(Collectors.toList());
            sNode.setConsumeNodeList(consumeNodes);

            subNodes.add(sNode);
        });
        graph.setSubscriptionNodeList(subNodes);

        return graph;
    }

}
