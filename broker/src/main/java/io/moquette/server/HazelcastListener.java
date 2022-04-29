package io.moquette.server;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import io.moquette.imhandler.IMHandler;
import io.moquette.interception.HazelcastInterceptHandler;
import io.moquette.interception.dispatch.DispatchPublish2Receives;
import io.moquette.interception.dispatch.DispatchPublishNotification2Receives;
import io.moquette.interception.dispatch.DispatchPublishRecall2Receives;
import io.moquette.interception.dispatch.DispatchPublishTransparent2Receives;
import io.moquette.interception.messages.*;
import io.moquette.spi.ClientSession;
import io.moquette.spi.impl.MessagesPublisher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HazelcastListener {
    private static final Logger LOG = LoggerFactory.getLogger(HazelcastListener.class);
    private final        Server server;

    public HazelcastListener(Server server) {
        this.server = server;
    }

    public void listenOnHazelCastMsg() {
        LOG.info("Subscribing to Hazelcast topic.");

        HazelcastInstance hz = server.getHazelcastInstance();
//        hz.<InterceptConnectMessage>getTopic(HazelcastInterceptHandler.TOPIC_CONNECT)
//            .addMessageListener(this::onConnectMessage);
//        hz.<InterceptPublishMessage>getTopic(HazelcastInterceptHandler.TOPIC_CONNECT_LOST)
//            .addMessageListener(this::onPublishMessage);
//        hz.<InterceptPublishMessage>getTopic(HazelcastInterceptHandler.TOPIC_DISCONNECT)
//            .addMessageListener(this::onPublishMessage);
//        hz.<InterceptPublishMessage>getTopic(HazelcastInterceptHandler.TOPIC_PUBLISH)
//            .addMessageListener(this::onPublishMessage);
//        hz.<InterceptPublishMessage>getTopic(HazelcastInterceptHandler.TOPIC_ACK)
//            .addMessageListener(this::onPublishMessage);


        hz.<DispatchPublish2Receives>getTopic(HazelcastInterceptHandler.TOPIC_PUBLISH)
            .addMessageListener(new MessageListener<DispatchPublish2Receives>() {
                @Override
                public void onMessage(Message<DispatchPublish2Receives> msg) {
                    try {
                        if (!msg.getPublishingMember().equals(server.getHazelcastInstance().getCluster().getLocalMember())) {
                            DispatchPublish2Receives hzMsg = msg.getMessageObject();
                            LOG.info("handle topic TOPIC_PUBLISH, sender={}, to={}, msgId={}",hzMsg.getSender(),hzMsg.getReceivers(),hzMsg.getMessageId());
                            MessagesPublisher publisher = IMHandler.getPublisher();
                            publisher.dispatchPublish2Receivers(hzMsg);
                        }
                    } catch (Exception ex) {
                        LOG.error("error handle hazelcast topic event TOPIC_PUBLISH", ex);
                    }
                }
            });
        hz.<DispatchPublishTransparent2Receives>getTopic(HazelcastInterceptHandler.TOPIC_PUBLISH_TRANSPARENT)
            .addMessageListener(new MessageListener<DispatchPublishTransparent2Receives>() {
                @Override
                public void onMessage(Message<DispatchPublishTransparent2Receives> msg) {
                    try {
                        if (!msg.getPublishingMember().equals(server.getHazelcastInstance().getCluster().getLocalMember())) {
                            DispatchPublishTransparent2Receives hzMsg = msg.getMessageObject();
                            LOG.info("handle topic TOPIC_PUBLISH_TRANSPARENT, pulltype={}, to={}, head={}, exClientId={}",
                                hzMsg.getPullType(),hzMsg.getReceivers(),hzMsg.getMessageHead(),hzMsg.getExceptClientId());
                            MessagesPublisher publisher = IMHandler.getPublisher();
                            publisher.dispatchPublishTransparentMessage2Receivers(hzMsg);
                        }
                    } catch (Exception ex) {
                        LOG.error("error handle hazelcast topic event TOPIC_PUBLISH_TRANSPARENT", ex);
                    }
                }
            });
        hz.<DispatchPublishNotification2Receives>getTopic(HazelcastInterceptHandler.TOPIC_PUBLISH_NOTIFICATION)
            .addMessageListener(new MessageListener<DispatchPublishNotification2Receives>() {
                @Override
                public void onMessage(Message<DispatchPublishNotification2Receives> msg) {
                    LOG.info("handle topic TOPIC_PUBLISH_RECALL");
                    try {
                        if (!msg.getPublishingMember().equals(server.getHazelcastInstance().getCluster().getLocalMember())) {
                            DispatchPublishNotification2Receives hzMsg = msg.getMessageObject();
                            LOG.info("handle topic TOPIC_PUBLISH_NOTIFICATION, from={}, to={}, head={}, exClientId={}, topic={}, pushContent={}",
                                hzMsg.getFromUser(),hzMsg.getReceiver(),hzMsg.getHead(),hzMsg.getExceptClientId(),hzMsg.getTopic(),hzMsg.getPushContent());
                            MessagesPublisher publisher = IMHandler.getPublisher();
                            publisher.dispatchPublishNotification(hzMsg);
                        }
                    } catch (Exception ex) {
                        LOG.error("error handle hazelcast topic event TOPIC_PUBLISH_NOTIFICATION", ex);
                    }
                }
            });
        hz.<DispatchPublishRecall2Receives>getTopic(HazelcastInterceptHandler.TOPIC_PUBLISH_RECALL)
            .addMessageListener(new MessageListener<DispatchPublishRecall2Receives>() {
                @Override
                public void onMessage(Message<DispatchPublishRecall2Receives> msg) {
                    LOG.info("handle topic TOPIC_PUBLISH_RECALL");
                    try {
                        if (!msg.getPublishingMember().equals(server.getHazelcastInstance().getCluster().getLocalMember())) {
                            DispatchPublishRecall2Receives hzMsg = msg.getMessageObject();
                            LOG.info("handle topic TOPIC_PUBLISH_RECALL, from={}, to={}, msgUid={}, exClientId={}",
                                hzMsg.getOperatorId(),hzMsg.getReceivers(),hzMsg.getMessageUid(),hzMsg.getExceptClientId());
                            MessagesPublisher publisher = IMHandler.getPublisher();
                            publisher.dispatchPublishRecall2ReceiversLocal(hzMsg);
                        }
                    } catch (Exception ex) {
                        LOG.error("error handle hazelcast topic connect event TOPIC_PUBLISH_RECALL", ex);
                    }
                }
            });

    }

    private void onConnectMessage(Message<InterceptConnectMessage> msg) {
        try {
            if (!msg.getPublishingMember().equals(server.getHazelcastInstance().getCluster().getLocalMember())) {
                InterceptConnectMessage hzMsg = msg.getMessageObject();
                LOG.info("{} connect from hazelcast for client: {}", hzMsg.getUsername(), hzMsg.getClientID());

                server.getStore().sessionsStore().loadUserSession(hzMsg.getUsername(), hzMsg.getClientID());
                ClientSession clientSession = server.getStore().sessionsStore().sessionForClient(hzMsg.getClientID());
                if (clientSession != null) {
                    server.getStore().sessionsStore().updateExistSession(hzMsg.getUsername(), hzMsg.getClientID(), null, hzMsg.isClearSession());
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

                MqttQoS                   qos         = MqttQoS.valueOf(hzMsg.getQos());
                MqttFixedHeader           fixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, false, qos, false, 0);
                MqttPublishVariableHeader varHeader   = new MqttPublishVariableHeader(hzMsg.getTopic(), 0);
                payload = Unpooled.wrappedBuffer(hzMsg.getPayload());
                MqttPublishMessage publishMessage = new MqttPublishMessage(fixedHeader, varHeader, payload);

                final int messageID = publishMessage.variableHeader().packetId();
                if (!server.m_initialized) {
                    LOG.error("Moquette is not started, internal message cannot be published. CId={}, messageId={}", hzMsg.getClientId(),
                        messageID);
                    throw new IllegalStateException("Can't publish on a server is not yet started");
                }
                server.getProcessor().internalPublish(publishMessage, hzMsg.getClientId(), hzMsg.getUsername());
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
