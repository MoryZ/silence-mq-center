package org.mapstruct.extensions.spring.converter;

import com.old.silence.mq.center.domain.model.Topic;
import com.old.silence.mq.center.dto.TopicCommand;
import javax.annotation.processing.Generated;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Component;

@Generated("org.mapstruct.extensions.spring.converter.ConversionServiceAdapterGenerator")
@Component
public class ConversionServiceAdapter {
  private static final TypeDescriptor TYPE_DESCRIPTOR_COM_OLD_SILENCE_MQ_CENTER_DTO_TOPICCOMMAND = TypeDescriptor.valueOf(TopicCommand.class);

  private static final TypeDescriptor TYPE_DESCRIPTOR_COM_OLD_SILENCE_MQ_CENTER_DOMAIN_MODEL_TOPIC = TypeDescriptor.valueOf(Topic.class);

  private final ConversionService conversionService;

  public ConversionServiceAdapter(@Lazy final ConversionService conversionService) {
    this.conversionService = conversionService;
  }

  public Topic mapTopicCommandToTopic(final TopicCommand source) {
    return (Topic) conversionService.convert(source, TYPE_DESCRIPTOR_COM_OLD_SILENCE_MQ_CENTER_DTO_TOPICCOMMAND, TYPE_DESCRIPTOR_COM_OLD_SILENCE_MQ_CENTER_DOMAIN_MODEL_TOPIC);
  }
}
