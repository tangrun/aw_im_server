DROP TABLE IF EXISTS `t_messages_history`;
CREATE TABLE `t_messages_history` (
                                      `id` int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,
                                      `_mid` bigint(20) NOT NULL,
                                      `_from` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
                                      `_type` tinyint NOT NULL DEFAULT 0,
                                      `_target` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
                                      `_line` int(11) NOT NULL DEFAULT 0,
                                      `_data` BLOB NOT NULL,
                                      `_searchable_key` TEXT DEFAULT NULL,
                                      `_dt` DATETIME NOT NULL,
                                      `_content_type` int(11) NOT NULL DEFAULT 0,
                                      `_to` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
                                      `_remote_address_ip` varchar(48) DEFAULT NULL,
                                      `_remote_address_port` varchar(8) DEFAULT NULL,
                                      `_remote_address_region` varchar(64) DEFAULT NULL,
                                      INDEX `message_uid_index` (`_mid` DESC),
                                      INDEX `messages2_conv_index`(`_type`, `_target`, `_line`)
)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;

alter table `t_messages` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_0` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_1` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_2` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_3` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_4` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_5` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_6` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_7` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_8` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_9` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_10` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_11` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_12` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_13` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_14` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_15` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_16` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_17` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_18` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_19` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_20` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_21` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_22` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_23` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_24` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_25` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_26` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_27` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_28` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_29` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_30` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_31` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_32` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_33` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_34` add column `_remote_address_ip` varchar(48) DEFAULT NULL;
alter table `t_messages_35` add column `_remote_address_ip` varchar(48) DEFAULT NULL;

alter table `t_messages` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_0` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_1` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_2` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_3` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_4` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_5` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_6` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_7` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_8` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_9` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_10` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_11` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_12` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_13` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_14` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_15` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_16` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_17` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_18` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_19` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_20` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_21` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_22` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_23` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_24` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_25` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_26` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_27` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_28` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_29` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_30` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_31` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_32` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_33` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_34` add column `_remote_address_port` varchar(8) DEFAULT NULL;
alter table `t_messages_35` add column `_remote_address_port` varchar(8) DEFAULT NULL;

alter table `t_messages` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_0` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_1` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_2` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_3` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_4` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_5` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_6` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_7` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_8` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_9` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_10` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_11` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_12` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_13` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_14` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_15` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_16` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_17` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_18` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_19` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_20` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_21` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_22` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_23` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_24` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_25` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_26` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_27` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_28` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_29` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_30` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_31` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_32` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_33` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_34` add column `_remote_address_region` varchar(64) DEFAULT NULL;
alter table `t_messages_35` add column `_remote_address_region` varchar(64) DEFAULT NULL;
