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

package io.moquette;

import java.io.File;

public final class BrokerConstants {

    public static final String IP_REGION_XDB_PATH = "ip_region_xdb_path";

    public static final String LAUNCH_TYPE = "launcher.type";

    public static final String FILE_SERVER_TOKEN_KEY = "file.server.token.key";
    public static final String FILE_SERVER_LOCAL_PORT = "file.server.local.port";
    public static final String FILE_SERVER_LOCAL_DIR = "file.server.local.dir";

    public static final String FILE_SERVER_UPLOAD_URL = "file.server.upload.url";
    public static final String FILE_SERVER_UPLOAD_PORT = "file.server.upload.port";
    public static final String FILE_SERVER_DOWNLOAD_URL = "file.server.download.url";


    public static final String INTERCEPT_HANDLER_PROPERTY_NAME = "intercept.handler";
    public static final String BROKER_INTERCEPTOR_THREAD_POOL_SIZE = "intercept.thread_pool.size";
    public static final String PERSISTENT_STORE_PROPERTY_NAME = "persistent_store";
    public static final String SERVER_IP_PROPERTY_NAME = "server.ip";
    public static final String PORT_PROPERTY_NAME = "port";
    public static final String HOST_PROPERTY_NAME = "host";
    public static final String HTTP_SERVER_PORT = "http_port";
    public static final String HTTP_LOCAL_PORT = "local_port";

    public static final String HTTP_ADMIN_PORT = "http.admin.port";
    public static final String DEFAULT_MOQUETTE_STORE_MAP_DB_FILENAME = "moquette_store.mapdb";
    public static final String DEFAULT_PERSISTENT_PATH = System.getProperty("user.dir") + File.separator
            + DEFAULT_MOQUETTE_STORE_MAP_DB_FILENAME;
    public static final String WSS_PORT_PROPERTY_NAME = "secure_websocket_port";
    public static final String SSL_PORT_PROPERTY_NAME = "ssl_port";
    public static final String JKS_PATH_PROPERTY_NAME = "jks_path";
    public static final String KEY_STORE_PASSWORD_PROPERTY_NAME = "key_store_password";
    public static final String KEY_MANAGER_PASSWORD_PROPERTY_NAME = "key_manager_password";
    public static final String AUTHORIZATOR_CLASS_NAME = "authorizator_class";
    public static final String DB_AUTHENTICATOR_DRIVER = "authenticator.db.driver";
    public static final String DB_AUTHENTICATOR_URL = "authenticator.db.url";
    public static final String DB_AUTHENTICATOR_QUERY = "authenticator.db.query";
    public static final String DB_AUTHENTICATOR_DIGEST = "authenticator.db.digest";
    public static final int PORT = 1883;
    public static final String DISABLED_PORT_BIND = "disabled";
    public static final String HOST = "0.0.0.0";
    public static final String NEED_CLIENT_AUTH = "need_client_auth";
    public static final String HAZELCAST_CLIENT_IP = "hazelcast.client.ip";
    public static final String HAZELCAST_CLIENT_PORT = "hazelcast.client.port";
    public static final String NETTY_SO_BACKLOG_PROPERTY_NAME = "netty.so_backlog";
    public static final String NETTY_SO_REUSEADDR_PROPERTY_NAME = "netty.so_reuseaddr";
    public static final String NETTY_TCP_NODELAY_PROPERTY_NAME = "netty.tcp_nodelay";
    public static final String NETTY_SO_KEEPALIVE_PROPERTY_NAME = "netty.so_keepalive";
    public static final String NETTY_CHANNEL_TIMEOUT_SECONDS_PROPERTY_NAME = "netty.channel_timeout.seconds";
    public static final String NETTY_EPOLL_PROPERTY_NAME = "netty.epoll";

    public static final String STORAGE_CLASS_NAME = "storage_class";

    public static final String QINIU_SERVER_URL= "qiniu.server_url";
	public static final String QINIU_ACCESS_KEY = "qiniu.access_key";
	public static final String QINIU_SECRET_KEY = "qiniu.secret_key";

    public static final String QINIU_BUCKET_GENERAL_NAME= "qiniu.bucket_general_name";
    public static final String QINIU_BUCKET_GENERAL_DOMAIN = "qiniu.bucket_general_domain";
    public static final String QINIU_BUCKET_IMAGE_NAME= "qiniu.bucket_image_name";
    public static final String QINIU_BUCKET_IMAGE_DOMAIN = "qiniu.bucket_image_domain";
    public static final String QINIU_BUCKET_VOICE_NAME= "qiniu.bucket_voice_name";
    public static final String QINIU_BUCKET_VOICE_DOMAIN = "qiniu.bucket_voice_domain";
    public static final String QINIU_BUCKET_VIDEO_NAME= "qiniu.bucket_video_name";
    public static final String QINIU_BUCKET_VIDEO_DOMAIN = "qiniu.bucket_video_domain";
    public static final String QINIU_BUCKET_FILE_NAME= "qiniu.bucket_file_name";
    public static final String QINIU_BUCKET_FILE_DOMAIN = "qiniu.bucket_file_domain";
    public static final String QINIU_BUCKET_STICKER_NAME= "qiniu.bucket_sticker_name";
    public static final String QINIU_BUCKET_STICKER_DOMAIN = "qiniu.bucket_sticker_domain";
    public static final String QINIU_BUCKET_MOMENTS_NAME= "qiniu.bucket_moments_name";
    public static final String QINIU_BUCKET_MOMENTS_DOMAIN = "qiniu.bucket_moments_domain";
    public static final String QINIU_BUCKET_PORTRAIT_NAME= "qiniu.bucket_portrait_name";
    public static final String QINIU_BUCKET_PORTRAIT_DOMAIN = "qiniu.bucket_portrait_domain";
    public static final String QINIU_BUCKET_FAVORITE_NAME= "qiniu.bucket_favorite_name";
    public static final String QINIU_BUCKET_FAVORITE_DOMAIN = "qiniu.bucket_favorite_domain";

    public static final String FILE_STORAGE_ROOT = "local.media.storage.root";
    public static final String FILE_STORAGE_REMOTE_SERVER_URL = "local.media.storage.remote_server_url";

    public static final String USER_QINIU = "media.server.use_qiniu";

    public static final String PUSH_ANDROID_SERVER_ADDRESS = "push.android.server.address";
    public static final String PUSH_IOS_SERVER_ADDRESS = "push.ios.server.address";

    public static final String MONITOR_Exception_Event_Address = "monitor.exception_event_address";


    public static final String USER_ONLINE_STATUS_CALLBACK = "user.online_status_callback";

    public static final String GROUP_INFO_UPDATE_CALLBACK = "group.group_info_update_callback";
    public static final String GROUP_MEMBER_UPDATE_CALLBACK = "group.group_member_update_callback";
    public static final String RELATION_UPDATE_CALLBACK = "relation.relation_update_callback";
    public static final String USER_INFO_UPDATE_CALLBACK = "user.user_info_update_callback";


    public static final String CHANNEL_INFO_UPDATE_CALLBACK = "channel.channel_info_update_callback";
    public static final String CHATROOM_INFO_UPDATE_CALLBACK = "chatroom.chatroom_info_update_callback";
    public static final String CHATROOM_MEMBER_UPDATE_CALLBACK = "chatroom.chatroom_member_update_callback";

    public static final String HTTP_SERVER_SECRET_KEY = "http.admin.secret_key";
    public static final String HTTP_SERVER_API_NO_CHECK_TIME = "http.admin.no_check_time";

    public static final String TOKEN_SECRET_KEY = "token.key";
    public static final String TOKEN_EXPIRE_TIME = "token.expire_time";

    public static final String EMBED_DB_PROPERTY_NAME = "embed.db";
    public static final String DB_AUTO_CLEAN_HISTORY_MESSAGES = "db.auto_clean_history_messages";
    public static final String DB_AUTO_CLEAN_HISTORY_MESSAGES_YEAR = "db.auto_clean_history_messages_year";
    public static final String DB_AUTO_CLEAN_HISTORY_MESSAGES_MONTH = "db.auto_clean_history_messages_month";
    public static final String DB_AUTO_CLEAN_HISTORY_MESSAGES_DAY = "db.auto_clean_history_messages_day";

    public static final String SENSITIVE_Filter_Type = "sensitive.filter.type";

    public static final String SENSITIVE_Remote_Server_URL = "sensitive.remote_server_url";
    public static final String SENSITIVE_Remote_Message_Type = "sensitive.remote_sensitive_message_type";
    public static final String SENSITIVE_Remote_Fail_When_Matched = "sensitive.remote_fail_when_matched";

    public static final String MESSAGE_Forward_Url = "message.forward.url";
    public static final String MESSAGE_Sensitive_Forward_Url = "message.sensitive.forward.url";
    public static final String MESSAGE_Forward_Types = "message.forward.types";
    public static final String MESSAGE_MentionMsg_Forward_Url = "message.mentionmsg.forward.url";


    public static final String SERVER_MULTI_ENDPOINT = "server.multi_endpoint";
    public static final String SERVER_MULTI_PC_ENDPOINT = "server.multi_pc_endpoint";
    public static final String SERVER_MULTI_PLATFROM_NOTIFICATION = "server.multi_platform_notification";
    public static final String SERVER_MOBILE_DEFAULT_SILENT_WHEN_PC_ONLINE = "server.mobile_default_silent_when_pc_online";
    public static final String SERVER_CLIENT_SUPPORT_KICKOFF_EVENT = "server.client_support_kickoff_event";

    public static final String MESSAGE_ROAMING = "message.roaming";
    public static final String MESSAGE_Compensate_Time_Limit = "message.compensate_time_limit";
    public static final String MESSAGE_Remote_History_Message = "message.remote_history_message";
    public static final String MESSAGE_Remote_Chatroom_History_Message = "message.chatroom_remote_history_message";

    public static final String MESSAGE_Max_Queue = "message.max_queue";

    public static final String MESSAGE_Disable_Stranger_Chat = "message.disable_stranger_chat";

    public static final String MESSAGE_Blacklist_Strategy = "message.blacklist.strategy";

    public static final String MESSAGE_NO_Forward_Admin_Message = "message.no_forward_admin_message";

    public static final String MESSAGE_Forward_With_Client_Info = "message.forward_with_client_info";
    public static final String ROBOT_Callback_With_Client_Info = "robot.callback_with_client_info";
    public static final String CHANNEL_Callback_With_Client_Info = "channel.callback_with_client_info";

    public static final String FRIEND_Disable_Search = "friend.disable_search";
    public static final String FRIEND_Disable_NickName_Search = "friend.disable_nick_name_search";
    public static final String FRIEND_Disable_Friend_Request = "friend.disable_friend_request";
    public static final String FRIEND_Repeat_Request_Duration = "friend.repeat_request_duration";
    public static final String FRIEND_Reject_Request_Duration = "friend.reject_request_duration";
    public static final String FRIEND_Request_Expiration_Duration = "friend.request_expiration_duration";
    public static final String FRIEND_New_Welcome_Message = "friend.new_welcome_message";

    public static final String CHATROOM_Participant_Idle_Time = "chatroom.participant_idle_time";
    public static final String CHATROOM_Rejoin_When_Active = "chatroom.rejoin_when_active";
    public static final String CHATROOM_Create_When_Not_Exist = "chatroom.create_when_not_exist";


    public static final String GROUP_Allow_Client_Custom_Operation_Notification = "group.allow_client_custom_operation_notification";
    public static final String GROUP_Allow_Robot_Custom_Operation_Notification = "group.allow_robot_custom_operation_notificatio";

    public static final String GROUP_Visible_Quit_Kickoff_Notification = "group.visible_quit_or_kickoff_notification";

    public static final String USER_HIDE_PROPERTIES = "user.hide_properties";
    public static final String USER_KEEP_DISPLAY_NAME_WHEN_DESTROY = "user.keep_display_name_when_destroy";

    public static final String SYNC_Data_Part_Size = "sync.data_part_size";

    public static final String MESSAGES_FORBIDDEN_CLIENT_SEND_TYPES = "message.forbidden_client_send_types";
    public static final String MESSAGES_BLACKLIST_EXCEPTION_TYPES = "message.blacklist_exception_types";
    public static final String MESSAGES_GROUP_MUTE_EXCEPTION_TYPES = "message.group_mute_exception_types";
    public static final String MESSAGES_GLOBAL_MUTE_EXCEPTION_TYPES = "message.global_mute_exception_types";

    public static final String MESSAGES_DISABLE_REMOTE_SEARCH = "message.disable_remote_search";
    public static final String MESSAGES_ENCRYPT_MESSAGE_CONTENT = "message.encrypt_message_content";


    public static final String MESSAGES_RECALL_TIME_LIMIT = "message.recall_time_limit";
    public static final String MESSAGES_DISABLE_GROUP_MANAGER_RECALL = "message.disable_group_manager_recall";

    public static final String HTTP_ADMIN_RATE_LIMIT = "http.admin.rate_limit";
    public static final String HTTP_ROBOT_RATE_LIMIT = "http.robot.rate_limit";
    public static final String HTTP_CHANNEL_RATE_LIMIT = "http.channel.rate_limit";
    public static final String CLIENT_REQUEST_RATE_LIMIT = "client.request_rate_limit";

    private BrokerConstants() {
    }
}
