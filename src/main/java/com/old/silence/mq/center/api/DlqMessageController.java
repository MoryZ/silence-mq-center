package com.old.silence.mq.center.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.old.silence.mq.center.dto.DlqMessageRequest;
import com.old.silence.mq.center.dto.DlqMessageResendResult;
import com.old.silence.mq.center.dto.MessagePage;
import com.old.silence.mq.center.dto.MessageQuery;
import com.old.silence.mq.center.domain.service.DlqMessageService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dlqMessage")
public class DlqMessageController {


    private final DlqMessageService dlqMessageService;


    public DlqMessageController(DlqMessageService dlqMessageService) {
        this.dlqMessageService = dlqMessageService;
    }

    @PostMapping(value = "/queryDlqMessageByConsumerGroup")
    public MessagePage queryDlqMessageByConsumerGroup(@RequestBody MessageQuery query) throws Exception {
        return dlqMessageService.queryDlqMessageByPage(query);
    }

    @PostMapping(value = "/batchResendDlqMessage")
    public List<DlqMessageResendResult> batchResendDlqMessage(@RequestBody List<DlqMessageRequest> dlqMessages) throws Exception {
        return dlqMessageService.batchResendDlqMessage(dlqMessages);
    }

}
