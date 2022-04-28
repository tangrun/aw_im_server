package io.moquette.interception.dispatch;

import java.io.Serializable;
import java.util.Collection;

public class DispatchPublishRecall2Receives implements Serializable {
    long messageUid; String operatorId; Collection<String> receivers; String exceptClientId;

    public DispatchPublishRecall2Receives(long messageUid, String operatorId, Collection<String> receivers, String exceptClientId) {
        this.messageUid = messageUid;
        this.operatorId = operatorId;
        this.receivers = receivers;
        this.exceptClientId = exceptClientId;
    }

    public long getMessageUid() {
        return messageUid;
    }

    public void setMessageUid(long messageUid) {
        this.messageUid = messageUid;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public Collection<String> getReceivers() {
        return receivers;
    }

    public void setReceivers(Collection<String> receivers) {
        this.receivers = receivers;
    }

    public String getExceptClientId() {
        return exceptClientId;
    }

    public void setExceptClientId(String exceptClientId) {
        this.exceptClientId = exceptClientId;
    }
}
