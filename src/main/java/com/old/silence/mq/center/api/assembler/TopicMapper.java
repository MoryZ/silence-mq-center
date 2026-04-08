package com.old.silence.mq.center.api.assembler;

import org.mapstruct.Mapper;
import org.springframework.core.convert.converter.Converter;
import com.old.silence.core.mapstruct.MapStructSpringConfig;
import com.old.silence.mq.center.domain.model.Topic;
import com.old.silence.mq.center.dto.TopicCommand;

/**
 * @author moryzang
 */
@Mapper(uses = MapStructSpringConfig.class)
public interface TopicMapper extends Converter<TopicCommand, Topic> {
}
