package io.moquette.server;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HazelcastPublishListener implements   MessageListener<InterceptPublishMessage> {
    private static final Logger log = LoggerFactory.getLogger(HazelcastPublishListener.class);
    private final Server server;

    public HazelcastPublishListener(Server server) {
        this.server = server;
    }

    @Override
    public void onMessage(Message<InterceptPublishMessage> msg) {
        ByteBuf payload = null;
        try {
            if (!msg.getPublishingMember().equals(server.getHazelcastInstance().getCluster().getLocalMember())) {
                InterceptPublishMessage hzMsg = msg.getMessageObject();
                log.info("{} received from hazelcast for topic {} message: {}", hzMsg.getClientId(), hzMsg.getTopic(),
                    hzMsg.getPayload());
                // TODO pass forward this information in somehow publishMessage.setLocal(false);

                MqttQoS qos = MqttQoS.valueOf(hzMsg.getQos());
                MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, false, qos, false, 0);
                MqttPublishVariableHeader varHeader = new MqttPublishVariableHeader(hzMsg.getTopic(), 0);
                payload = Unpooled.wrappedBuffer(hzMsg.getPayload());
                MqttPublishMessage publishMessage = new MqttPublishMessage(fixedHeader, varHeader, payload);

                final int messageID = publishMessage.variableHeader().packetId();
                if (!server.m_initialized) {
                    log.error("Moquette is not started, internal message cannot be published. CId={}, messageId={}", hzMsg.getClientId(),
                        messageID);
                    throw new IllegalStateException("Can't publish on a server is not yet started");
                }
                server.getProcessor().internalPublish(publishMessage, hzMsg.getClientId());
            }
        } catch (Exception ex) {
            log.error("error polling hazelcast msg queue", ex);
        } finally {
            ReferenceCountUtil.release(payload);
        }
    }
}
