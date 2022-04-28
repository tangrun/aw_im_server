package io.moquette.interception.dispatch;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public class DispatchPublishTransparent2Receives implements Serializable {
    long messageHead; List<String> receivers; int pullType; String exceptClientId;

    public DispatchPublishTransparent2Receives(long messageHead, List<String> receivers, int pullType, String exceptClientId) {
        this.messageHead = messageHead;
        this.receivers = receivers;
        this.pullType = pullType;
        this.exceptClientId = exceptClientId;
    }

    public long getMessageHead() {
        return messageHead;
    }

    public void setMessageHead(long messageHead) {
        this.messageHead = messageHead;
    }

    public List<String> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<String> receivers) {
        this.receivers = receivers;
    }

    public int getPullType() {
        return pullType;
    }

    public void setPullType(int pullType) {
        this.pullType = pullType;
    }

    public String getExceptClientId() {
        return exceptClientId;
    }

    public void setExceptClientId(String exceptClientId) {
        this.exceptClientId = exceptClientId;
    }
}
