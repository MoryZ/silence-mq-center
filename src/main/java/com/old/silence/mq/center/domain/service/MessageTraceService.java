

package com.old.silence.mq.center.domain.service;

import com.old.silence.mq.center.domain.model.MessageTraceView;
import com.old.silence.mq.center.domain.model.trace.MessageTraceGraph;

import java.util.List;
public interface MessageTraceService {

    List<MessageTraceView> queryMessageTraceKey(final String key);

    List<MessageTraceView> queryMessageTraceByTopicAndKey(final String topic, final String key);

    MessageTraceGraph queryMessageTraceGraph(final String key, final String traceTopic);
}
