package io.moquette.interception.dispatch;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DispatchPublish2Receives implements Serializable {
    String       sender;
    int          conversationType;
    String       target;
    int          line;
    long         messageId;
    Map<String,Long> receivers;
    String       pushContent;
    String       pushData;
    String       exceptClientId;
    int pullType;
    int          messageContentType;
    long         serverTime;
    int          mentionType;
    List<String> mentionTargets;
    int          persistFlag;
    boolean      directing;

    public DispatchPublish2Receives(String sender, int conversationType, String target, int line, long messageId, Map<String,Long> receivers,
                                    String pushContent, String pushData, String exceptClientId, int pullType,
                                    int messageContentType, long serverTime, int mentionType, List<String> mentionTargets, int persistFlag, boolean directing) {
        this.sender = sender;
        this.conversationType = conversationType;
        this.target = target;
        this.line = line;
        this.messageId = messageId;
        this.receivers = receivers;
        this.pushContent = pushContent;
        this.pushData = pushData;
        this.exceptClientId = exceptClientId;
        this.pullType = pullType;
        this.messageContentType = messageContentType;
        this.serverTime = serverTime;
        this.mentionType = mentionType;
        this.mentionTargets = mentionTargets;
        this.persistFlag = persistFlag;
        this.directing = directing;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public int getConversationType() {
        return conversationType;
    }

    public void setConversationType(int conversationType) {
        this.conversationType = conversationType;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public Map<String,Long> getReceivers() {
        return receivers;
    }

    public void setReceivers(Map<String,Long> receivers) {
        this.receivers = receivers;
    }

    public String getPushContent() {
        return pushContent;
    }

    public void setPushContent(String pushContent) {
        this.pushContent = pushContent;
    }

    public String getPushData() {
        return pushData;
    }

    public void setPushData(String pushData) {
        this.pushData = pushData;
    }

    public String getExceptClientId() {
        return exceptClientId;
    }

    public void setExceptClientId(String exceptClientId) {
        this.exceptClientId = exceptClientId;
    }

    public int getPullType() {
        return pullType;
    }

    public void setPullType(int pullType) {
        this.pullType = pullType;
    }

    public int getMessageContentType() {
        return messageContentType;
    }

    public void setMessageContentType(int messageContentType) {
        this.messageContentType = messageContentType;
    }

    public long getServerTime() {
        return serverTime;
    }

    public void setServerTime(long serverTime) {
        this.serverTime = serverTime;
    }

    public int getMentionType() {
        return mentionType;
    }

    public void setMentionType(int mentionType) {
        this.mentionType = mentionType;
    }

    public List<String> getMentionTargets() {
        return mentionTargets;
    }

    public void setMentionTargets(List<String> mentionTargets) {
        this.mentionTargets = mentionTargets;
    }

    public int getPersistFlag() {
        return persistFlag;
    }

    public void setPersistFlag(int persistFlag) {
        this.persistFlag = persistFlag;
    }

    public boolean isDirecting() {
        return directing;
    }

    public void setDirecting(boolean directing) {
        this.directing = directing;
    }
}
