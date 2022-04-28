package io.moquette.server;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import io.moquette.interception.HazelcastInterceptHandler;
import io.moquette.interception.messages.*;
import io.moquette.spi.ClientSession;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.moquette.server.ConnectionDescriptor.ConnectionState.INTERCEPTORS_NOTIFIED;
import static io.moquette.server.ConnectionDescriptor.ConnectionState.MESSAGES_DROPPED;

public class HazelcastListener {
    private static final Logger LOG = LoggerFactory.getLogger(HazelcastListener.class);
    private final Server server;

    public HazelcastListener(Server server) {
        this.server = server;
    }

    public void listenOnHazelCastMsg() {
        LOG.info("Subscribing to Hazelcast topic.");

        HazelcastInstance hz = server.getHazelcastInstance();
        hz.<InterceptConnectMessage>getTopic(HazelcastInterceptHandler.TOPIC_CONNECT)
            .addMessageListener(this::onConnectMessage);
        hz.<InterceptPublishMessage>getTopic(HazelcastInterceptHandler.TOPIC_CONNECT_LOST)
            .addMessageListener(this::onPublishMessage);
        hz.<InterceptPublishMessage>getTopic(HazelcastInterceptHandler.TOPIC_DISCONNECT)
            .addMessageListener(this::onPublishMessage);
        hz.<InterceptPublishMessage>getTopic(HazelcastInterceptHandler.TOPIC_PUBLISH)
            .addMessageListener(this::onPublishMessage);
        hz.<InterceptPublishMessage>getTopic(HazelcastInterceptHandler.TOPIC_ACK)
            .addMessageListener(this::onPublishMessage);

    }
    private void onConnectMessage(Message<InterceptConnectMessage> msg) {
        try {
            if (!msg.getPublishingMember().equals(server.getHazelcastInstance().getCluster().getLocalMember())) {
                InterceptConnectMessage hzMsg = msg.getMessageObject();
                LOG.info("{} connect from hazelcast for client: {}", hzMsg.getUsername(), hzMsg.getClientID());

                server.getStore().sessionsStore().loadUserSession(hzMsg.getUsername(), hzMsg.getClientID());
                ClientSession clientSession = server.getStore().sessionsStore().sessionForClient(hzMsg.getClientID());
                if (clientSession != null){
                    server.getStore().sessionsStore().updateExistSession(hzMsg.getUsername(), hzMsg.getClientID(),null, hzMsg.isClearSession());
                }
            }
        } catch (Exception ex) {
            LOG.error("error handle hazelcast connect event", ex);
        }
    }

    private void onDisconnectMessage(Message<InterceptDisconnectMessage> msg) {
        try {
            if (!msg.getPublishingMember().equals(server.getHazelcastInstance().getCluster().getLocalMember())) {
                InterceptDisconnectMessage hzMsg = msg.getMessageObject();
                LOG.info("{} connect from hazelcast for client: {}", hzMsg.getUsername(), hzMsg.getClientID());

                ClientSession clientSession = server.getStore().sessionsStore().sessionForClient(hzMsg.getClientID());
                if (hzMsg.)
                clientSession.cleanSession();

                server.getStore().sessionsStore().loadUserSession(hzMsg.getUsername(), hzMsg.getClientID());
            }
        } catch (Exception ex) {
            LOG.error("error handle hazelcast disconnect event", ex);
        }
    }

    private void onConnectLostMessage(Message<InterceptConnectionLostMessage> msg) {
        try {
            if (!msg.getPublishingMember().equals(server.getHazelcastInstance().getCluster().getLocalMember())) {
                InterceptConnectionLostMessage hzMsg = msg.getMessageObject();
                LOG.info("{} connect from hazelcast for client: {}", hzMsg.getUsername(), hzMsg.getClientID());

                server.getStore().sessionsStore().loadUserSession(hzMsg.getUsername(), hzMsg.getClientID());
            }
        } catch (Exception ex) {
            LOG.error("error handle hazelcast connect lost event", ex);
        }
    }

    private void onPublishMessage(Message<InterceptPublishMessage> msg) {
        ByteBuf payload = null;
        try {
            if (!msg.getPublishingMember().equals(server.getHazelcastInstance().getCluster().getLocalMember())) {
                InterceptPublishMessage hzMsg = msg.getMessageObject();
                LOG.info("{} received from hazelcast for topic {} message: {}", hzMsg.getClientId(), hzMsg.getTopic(),
                    hzMsg.getPayload());
                // TODO pass forward this information in somehow publishMessage.setLocal(false);

                MqttQoS qos = MqttQoS.valueOf(hzMsg.getQos());
                MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, false, qos, false, 0);
                MqttPublishVariableHeader varHeader = new MqttPublishVariableHeader(hzMsg.getTopic(), 0);
                payload = Unpooled.wrappedBuffer(hzMsg.getPayload());
                MqttPublishMessage publishMessage = new MqttPublishMessage(fixedHeader, varHeader, payload);

                final int messageID = publishMessage.variableHeader().packetId();
                if (!server.m_initialized) {
                    LOG.error("Moquette is not started, internal message cannot be published. CId={}, messageId={}", hzMsg.getClientId(),
                        messageID);
                    throw new IllegalStateException("Can't publish on a server is not yet started");
                }
                server.onApiMessage();
                server.getProcessor().internalPublish(publishMessage, hzMsg.getClientId());
            }
        } catch (Exception ex) {
            LOG.error("error polling hazelcast msg queue", ex);
        } finally {
            ReferenceCountUtil.release(payload);
        }
    }

    private void onAckMessage(Message<InterceptAcknowledgedMessage> msg) {

    }

}
