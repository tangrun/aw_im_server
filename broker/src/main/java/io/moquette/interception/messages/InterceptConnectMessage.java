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

import io.netty.handler.codec.mqtt.MqttConnectMessage;

public class InterceptConnectMessage implements InterceptMessage {

    private final String clientID;
    private final String username;
    private final byte[] password;
    private final boolean clearSession;

    public InterceptConnectMessage(String clientID, String username, byte[] password, boolean clearSession) {
        this.clientID = clientID;
        this.username = username;
        this.password = password;
        this.clearSession = clearSession;
    }

    public InterceptConnectMessage(MqttConnectMessage msg) {
        this.clientID = msg.payload().clientIdentifier();
        this.username = msg.payload().userName();
        this.password = msg.payload().password();
        this.clearSession =msg.variableHeader().isCleanSession();
    }

    public String getClientID() {
        return clientID;
    }

    public String getUsername() {
        return username;
    }

    public byte[] getPassword() {
        return password;
    }

    public boolean isClearSession() {
        return clearSession;
    }
}
