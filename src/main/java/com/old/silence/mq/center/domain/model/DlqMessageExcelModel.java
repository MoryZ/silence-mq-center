

package com.old.silence.mq.center.domain.model;


import org.apache.rocketmq.common.message.MessageExt;
import com.google.common.base.Charsets;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;


public class DlqMessageExcelModel implements Serializable {

    private String topic;

    private String msgId;

    private String bornHost;

    private String bornTimestamp;

    private String storeTimestamp;

    private int reconsumeTimes;

    private String properties;


    private String messageBody;


    private int bodyCRC;


    private String exception;

    public DlqMessageExcelModel() {
    }

    public DlqMessageExcelModel(MessageExt messageExt) {
        this.topic = messageExt.getTopic();
        this.msgId = messageExt.getMsgId();
        this.bornHost = messageExt.getBornHostString();
        this.bornTimestamp = format(messageExt.getBornTimestamp());
        this.storeTimestamp = format(messageExt.getStoreTimestamp());
        this.reconsumeTimes = messageExt.getReconsumeTimes();
        this.properties = messageExt.getProperties().toString();
        this.messageBody = new String(messageExt.getBody(), Charsets.UTF_8);
        this.bodyCRC = messageExt.getBodyCRC();
    }

    private String format(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return  instant.atOffset(ZoneOffset.UTC).format(formatter);
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getBornHost() {
        return bornHost;
    }

    public void setBornHost(String bornHost) {
        this.bornHost = bornHost;
    }

    public String getBornTimestamp() {
        return bornTimestamp;
    }

    public void setBornTimestamp(String bornTimestamp) {
        this.bornTimestamp = bornTimestamp;
    }

    public String getStoreTimestamp() {
        return storeTimestamp;
    }

    public void setStoreTimestamp(String storeTimestamp) {
        this.storeTimestamp = storeTimestamp;
    }

    public int getReconsumeTimes() {
        return reconsumeTimes;
    }

    public void setReconsumeTimes(int reconsumeTimes) {
        this.reconsumeTimes = reconsumeTimes;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public int getBodyCRC() {
        return bodyCRC;
    }

    public void setBodyCRC(int bodyCRC) {
        this.bodyCRC = bodyCRC;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }
}
