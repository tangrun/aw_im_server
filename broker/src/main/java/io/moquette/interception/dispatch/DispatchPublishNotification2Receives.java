package io.moquette.interception.dispatch;

import java.io.Serializable;

public class DispatchPublishNotification2Receives implements Serializable {
    String topic; String receiver; long head; String fromUser; String pushContent; String exceptClientId;

    public DispatchPublishNotification2Receives(String topic, String receiver, long head, String fromUser, String pushContent, String exceptClientId) {
        this.topic = topic;
        this.receiver = receiver;
        this.head = head;
        this.fromUser = fromUser;
        this.pushContent = pushContent;
        this.exceptClientId = exceptClientId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public long getHead() {
        return head;
    }

    public void setHead(long head) {
        this.head = head;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getPushContent() {
        return pushContent;
    }

    public void setPushContent(String pushContent) {
        this.pushContent = pushContent;
    }

    public String getExceptClientId() {
        return exceptClientId;
    }

    public void setExceptClientId(String exceptClientId) {
        this.exceptClientId = exceptClientId;
    }
}
