/*
 * Copyright (c) 2012-2017 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.moquette.interception.messages;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.mqtt.MqttPublishMessage;

import java.io.Serializable;

import static io.moquette.spi.impl.Utils.readBytesAndRewind;

public class InterceptPublishMessage implements InterceptMessage , Serializable {

    private final String username;
    private final String clientId;
    private final int qos;
    private final byte[] payload;
    private final String topic;

    public InterceptPublishMessage(MqttPublishMessage msg, String clientID, String username) {
        this.username = username;
        this.clientId = clientID;
        this.topic = msg.variableHeader().topicName();
        this.qos = msg.fixedHeader().qosLevel().ordinal();
        this.payload = readBytesAndRewind(msg.payload());
    }


    public String getUsername() {
        return username;
    }

    public String getClientId() {
        return clientId;
    }

    public int getQos() {
        return qos;
    }

    public byte[] getPayload() {
        return payload;
    }

    public String getTopic() {
        return topic;
    }
}
