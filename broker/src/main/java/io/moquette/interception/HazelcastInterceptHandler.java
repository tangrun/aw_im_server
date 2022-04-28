package io.moquette.interception;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import io.moquette.interception.messages.*;
import io.moquette.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HazelcastInterceptHandler
//    extends AbstractInterceptHandler
{

    public static final String TOPIC_PUBLISH ="pub";
    public static final String TOPIC_PUBLISH_TRANSPARENT ="pub_trans";
    public static final String TOPIC_PUBLISH_NOTIFICATION ="pub_notif";
    public static final String TOPIC_PUBLISH_RECALL ="pub_notif";

//    public static final String TOPIC_PUBLISH ="pub";
//    public static final String TOPIC_ACK ="ack";
//    public static final String TOPIC_CONNECT ="con";
//    public static final String TOPIC_DISCONNECT ="dis_con";
//    public static final String TOPIC_CONNECT_LOST ="con_lost";
//
//    private static final Logger LOG = LoggerFactory.getLogger(HazelcastInterceptHandler.class);
//    private final HazelcastInstance hz;
//
//    public HazelcastInterceptHandler(Server server) {
//        this.hz = server.getHazelcastInstance();
//    }
//
//    @Override
//    public String getID() {
//        return HazelcastInterceptHandler.class.getName() + "@" + hz.getName();
//    }
//
//    @Override
//    public void onPublish(InterceptPublishMessage msg) {
//        LOG.info("{} {} publish message: {}",msg.getUsername(), msg.getClientId(), msg.getTopic());
//        ITopic<InterceptPublishMessage> topic = hz.getTopic(TOPIC_PUBLISH);
//        topic.publish(msg);
//    }

//    @Override
//    public void onMessageAcknowledged(InterceptAcknowledgedMessage msg) {
//        LOG.info("{} {} message ack message: {}",msg.getUsername(), msg.getClientId(), msg.getTopic());
//        ITopic<InterceptPublishMessage> topic = hz.getTopic(TOPIC_PUBLISH);
//        topic.publish(msg);
//    }
//
//    @Override
//    public void onConnect(InterceptConnectMessage msg) {
//        LOG.info("{} {} connect message", msg.getUsername(), msg.getClientID());
//        ITopic<InterceptConnectMessage> topic = hz.getTopic(TOPIC_CONNECT);
//        topic.publish(msg);
//    }
//
//    @Override
//    public void onDisconnect(InterceptDisconnectMessage msg) {
//        LOG.info("{} {} disconnect message", msg.getUsername(), msg.getClientID());
//        ITopic<InterceptDisconnectMessage> topic = hz.getTopic(TOPIC_DISCONNECT);
//        topic.publish(msg);
//    }
//
//    @Override
//    public void onConnectionLost(InterceptConnectionLostMessage msg) {
//        LOG.info("{} {} connect message", msg.getUsername(), msg.getClientID());
//        ITopic<InterceptConnectionLostMessage> topic = hz.getTopic(TOPIC_CONNECT_LOST);
//        topic.publish(msg);
//    }
}
