package com.old.silence.mq.center.api.assembler;

import com.old.silence.mq.center.domain.model.Topic;
import com.old.silence.mq.center.dto.TopicCommand;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor"
)
@Component
public class TopicMapperImpl implements TopicMapper {

    @Override
    public Topic convert(TopicCommand source) {
        if ( source == null ) {
            return null;
        }

        Topic topic = new Topic();

        topic.setTopicName( source.getTopicName() );
        topic.setClusterName( source.getClusterName() );
        topic.setReadQueueNums( source.getReadQueueNums() );
        topic.setWriteQueueNums( source.getWriteQueueNums() );
        topic.setMessageType( source.getMessageType() );
        topic.setBrokerAddr( source.getBrokerAddr() );
        topic.setSystemTopic( source.getSystemTopic() );

        return topic;
    }
}
