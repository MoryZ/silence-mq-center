package com.old.silence.mq.center.api;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.old.silence.mq.center.domain.model.DlqMessageExcelModel;
import com.old.silence.mq.center.domain.model.DlqMessageRequest;
import com.old.silence.mq.center.domain.model.DlqMessageResendResult;
import com.old.silence.mq.center.domain.model.MessagePage;
import com.old.silence.mq.center.domain.model.request.MessageQuery;
import com.old.silence.mq.center.domain.service.DlqMessageService;
import com.old.silence.mq.center.exception.ServiceException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dlqMessage")
public class DlqMessageController {


    private static final Logger log = LoggerFactory.getLogger(DlqMessageController.class);
    private final DlqMessageService dlqMessageService;

    private final MQAdminExt mqAdminExt;

    public DlqMessageController(DlqMessageService dlqMessageService, MQAdminExt mqAdminExt) {
        this.dlqMessageService = dlqMessageService;
        this.mqAdminExt = mqAdminExt;
    }

    @PostMapping(value = "/queryDlqMessageByConsumerGroup")
    public MessagePage queryDlqMessageByConsumerGroup(@RequestBody MessageQuery query) {
        return dlqMessageService.queryDlqMessageByPage(query);
    }

    @GetMapping(value = "/exportDlqMessage")
    public void exportDlqMessage(HttpServletResponse response, @RequestParam String consumerGroup,
                                 @RequestParam String msgId) {
        MessageExt messageExt = null;
        try {
            String topic = MixAll.DLQ_GROUP_TOPIC_PREFIX + consumerGroup;
            messageExt = mqAdminExt.viewMessage(topic, msgId);
        } catch (Exception e) {
            throw new ServiceException(-1, String.format("Failed to query message by Id: %s", msgId));
        }
        DlqMessageExcelModel excelModel = new DlqMessageExcelModel(messageExt);
        try {
            //ExcelUtil.writeExcel(response, Lists.newArrayList(excelModel), "dlq", "dlq", DlqMessageExcelModel.class);
        } catch (Exception e) {
            throw new ServiceException(-1, String.format("export dlq message failed!"));
        }
    }

    @PostMapping(value = "/batchResendDlqMessage")
    public List<DlqMessageResendResult> batchResendDlqMessage(@RequestBody List<DlqMessageRequest> dlqMessages) {
        return dlqMessageService.batchResendDlqMessage(dlqMessages);
    }

    @PostMapping(value = "/batchExportDlqMessage")
    public void batchExportDlqMessage(HttpServletResponse response, @RequestBody List<DlqMessageRequest> dlqMessages) {
        List<DlqMessageExcelModel> dlqMessageExcelModelList = new ArrayList<>(dlqMessages.size());
        for (DlqMessageRequest dlqMessage : dlqMessages) {
            DlqMessageExcelModel excelModel = new DlqMessageExcelModel();
            try {
                String topic = MixAll.DLQ_GROUP_TOPIC_PREFIX + dlqMessage.getConsumerGroup();
                MessageExt messageExt = mqAdminExt.viewMessage(topic, dlqMessage.getMsgId());
                excelModel = new DlqMessageExcelModel(messageExt);
            } catch (Exception e) {
                log.error("Failed to query message by Id:{}", dlqMessage.getMsgId(), e);
                excelModel.setMsgId(dlqMessage.getMsgId());
                excelModel.setException(e.getMessage());
            }
            dlqMessageExcelModelList.add(excelModel);
        }
        try {
            //ExcelUtil.writeExcel(response, dlqMessageExcelModelList, "dlqs", "dlqs", DlqMessageExcelModel.class);
        } catch (Exception e) {
            throw new ServiceException(-1, String.format("export dlq message failed!"));
        }

    }
}
