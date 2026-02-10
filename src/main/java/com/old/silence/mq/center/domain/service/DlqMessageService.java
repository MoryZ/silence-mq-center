package com.old.silence.mq.center.domain.service;

import com.old.silence.mq.center.domain.model.DlqMessageRequest;
import com.old.silence.mq.center.domain.model.DlqMessageResendResult;
import com.old.silence.mq.center.domain.model.MessagePage;
import com.old.silence.mq.center.domain.model.request.MessageQuery;

import java.util.List;


public interface DlqMessageService {

    MessagePage queryDlqMessageByPage(MessageQuery query);

    List<DlqMessageResendResult> batchResendDlqMessage(List<DlqMessageRequest> dlqMessages);
}
