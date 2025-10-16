

package com.old.silence.mq.center.domain.service;

import org.apache.rocketmq.common.Pair;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.protocol.body.ConsumeMessageDirectlyResult;
import org.apache.rocketmq.tools.admin.api.MessageTrack;
import com.old.silence.mq.center.domain.model.MessagePage;
import com.old.silence.mq.center.domain.model.MessageView;
import com.old.silence.mq.center.domain.model.request.MessageQuery;

import java.util.List;

public interface MessageService {
    /**
     * @param subject
     * @param msgId
     */
    Pair<MessageView, List<MessageTrack>> viewMessage(String subject, final String msgId);

    List<MessageView> queryMessageByTopicAndKey(final String topic, final String key);

    /**
     * @param topic
     * @param begin
     * @param end
     * org.apache.rocketmq.tools.command.message.PrintMessageSubCommand
     */
    List<MessageView> queryMessageByTopic(final String topic, final long begin,
        final long end);

    List<MessageTrack> messageTrackDetail(MessageExt msg);

    ConsumeMessageDirectlyResult consumeMessageDirectly(String topic, String msgId, String consumerGroup,
        String clientId);


    MessagePage queryMessageByPage(MessageQuery query);




}
