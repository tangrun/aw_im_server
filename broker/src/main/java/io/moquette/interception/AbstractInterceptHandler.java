package io.moquette.interception;

import io.moquette.interception.messages.*;

public abstract class AbstractInterceptHandler implements InterceptHandler {

    @Override
    public Class<?>[] getInterceptedMessageTypes() {
        return InterceptHandler.ALL_MESSAGE_TYPES;
    }

    @Override
    public void onConnect(InterceptConnectMessage msg) {
    }

    @Override
    public void onDisconnect(InterceptDisconnectMessage msg) {
    }

    @Override
    public void onConnectionLost(InterceptConnectionLostMessage msg) {
    }

    @Override
    public void onPublish(InterceptPublishMessage msg) {
    }

    @Override
    public void onMessageAcknowledged(InterceptAcknowledgedMessage msg) {
    }
}
