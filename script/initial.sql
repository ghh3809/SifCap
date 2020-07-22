CREATE TABLE `user` (
  `user_id` int(11) NOT NULL DEFAULT '0' COMMENT 'uid',
  `name` varchar(30) DEFAULT NULL COMMENT '昵称',
  `level` int(11) DEFAULT NULL COMMENT '等级',
  `exp` int(11) DEFAULT NULL COMMENT '经验',
  `previous_exp` int(11) DEFAULT NULL COMMENT '上一级经验',
  `next_exp` int(11) DEFAULT NULL COMMENT '下一级经验',
  `game_coin` bigint(20) DEFAULT NULL COMMENT '金币',
  `sns_coin` int(11) DEFAULT NULL COMMENT '爱心',
  `free_sns_coin` int(11) DEFAULT NULL COMMENT '免费爱心',
  `paid_sns_coin` int(11) DEFAULT NULL COMMENT '付费爱心',
  `social_point` bigint(20) DEFAULT NULL COMMENT '友情点',
  `unit_max` int(11) DEFAULT NULL COMMENT '社员上限',
  `waiting_unit_max` int(11) DEFAULT NULL COMMENT '预备教室上限',
  `current_energy` int(11) DEFAULT NULL COMMENT '当前LP',
  `energy_max` int(11) DEFAULT NULL COMMENT 'LP上限',
  `energy_full_time` datetime DEFAULT NULL COMMENT '体力满时间',
  `energy_full_need_time` int(11) DEFAULT NULL COMMENT '体力满秒数',
  `over_max_energy` int(11) DEFAULT NULL COMMENT '超上限体力',
  `training_energy` int(11) DEFAULT NULL COMMENT '练习券',
  `training_energy_max` int(11) DEFAULT NULL COMMENT '练习券上限',
  `friend_max` int(11) DEFAULT NULL COMMENT '好友上限',
  `invite_code` varchar(10) DEFAULT NULL COMMENT '用户id',
  `unlock_random_live_muse` int(11) DEFAULT NULL,
  `unlock_random_live_aqours` int(11) DEFAULT NULL,
  `insert_date` datetime DEFAULT NULL COMMENT '创建账号日期',
  `update_date` datetime DEFAULT NULL COMMENT '更新账号日期',
  `tutorial_state` int(11) DEFAULT NULL COMMENT '新手引导状态',
  `diamond_coin` int(11) DEFAULT NULL COMMENT '钻石',
  `crystal_coin` int(11) DEFAULT NULL COMMENT '水晶',
  `birth_month` int(11) DEFAULT NULL COMMENT '生日（月）',
  `birth_day` int(11) DEFAULT NULL COMMENT '生日（日）',
  `setting_award_id` int(11) DEFAULT NULL COMMENT '称号ID',
  `setting_background_id` int(11) DEFAULT NULL COMMENT '背景ID',
  `introduction` text DEFAULT NULL COMMENT '简介',
  `center_unit_info_json` text DEFAULT NULL COMMENT '头像信息',
  `navi_unit_info_json` text DEFAULT NULL COMMENT '看板信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `invite_code` (`invite_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='玩家信息';

CREATE TABLE `live_play` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `user_id` int(11) NOT NULL COMMENT '用户id',
  `live_difficulty_id` int(11) DEFAULT NULL COMMENT '歌曲id',
  `is_random` varchar(5) DEFAULT 'false' COMMENT '是否随机',
  `ac_flag` int(11) DEFAULT NULL COMMENT '是否ac',
  `swing_flag` int(11) DEFAULT NULL COMMENT '是否滑键',
  `perfect_cnt` int(11) DEFAULT NULL COMMENT 'perfect数量',
  `great_cnt` int(11) DEFAULT NULL COMMENT 'great数量',
  `good_cnt` int(11) DEFAULT NULL COMMENT 'good数量',
  `bad_cnt` int(11) DEFAULT NULL COMMENT 'bad数量',
  `miss_cnt` int(11) DEFAULT NULL COMMENT 'miss数量',
  `max_combo` int(11) DEFAULT NULL COMMENT '最大combo',
  `score_smile` int(11) DEFAULT NULL COMMENT 'smile分数',
  `score_cute` int(11) DEFAULT NULL COMMENT 'cute分数',
  `score_cool` int(11) DEFAULT NULL COMMENT 'cool分数',
  `event_id` int(11) DEFAULT '0' COMMENT '活动id',
  `love_cnt` int(11) DEFAULT NULL COMMENT '获得绊点',
  `exp_cnt` int(11) DEFAULT NULL COMMENT '获得EXP',
  `game_coin_cnt` int(11) DEFAULT NULL COMMENT '获得金币',
  `social_point_cnt` int(11) DEFAULT NULL COMMENT '获得友情点',
  `unit_list_json` text COMMENT '使用队伍',
  `play_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='演唱会记录';

CREATE TABLE `event_map` (
    `event_id` int(11) NOT NULL COMMENT '活动ID',
    `event_name` varchar(128) NOT NULL COMMENT '活动名称',
    PRIMARY KEY (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='活动名称对应表';

INSERT INTO `event_map` VALUES(0, '非活动');

CREATE TABLE `event_live_m` (
    `live_difficulty_id` int(11) NOT NULL COMMENT '歌曲难度ID',
    `live_setting_id` int(11) NOT NULL COMMENT '歌曲设置ID',
    PRIMARY KEY (`live_difficulty_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='活动歌曲信息';

CREATE TABLE `unit` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
    `user_id` int(11) NOT NULL COMMENT '用户id',
    `status` varchar(10) NOT NULL COMMENT '卡所在状态，active/waiting',
    `love` int(11) DEFAULT NULL COMMENT '当前绊点',
    `max_love` int(11) DEFAULT NULL COMMENT '最大绊点',
    `is_skill_level_max` tinyint DEFAULT NULL COMMENT '技能等级是否最大',
    `level` int(11) DEFAULT NULL COMMENT '成员等级',
    `insert_date` datetime DEFAULT NULL COMMENT '获取时间',
    `unit_skill_level` int(11) DEFAULT NULL COMMENT '技能等级',
    `unit_skill_exp` int(11) DEFAULT NULL COMMENT '技能经验',
    `max_hp` int(11) DEFAULT NULL COMMENT '最大hp',
    `display_rank` int(11) DEFAULT NULL COMMENT '显示rank',
    `unit_removable_skill_capacity` int(11) DEFAULT NULL COMMENT '宝石容量',
    `unit_owning_user_id` bigint DEFAULT NULL COMMENT '获取id',
    `is_level_max` tinyint DEFAULT NULL COMMENT '等级是否最大',
    `max_rank` int(11) DEFAULT NULL COMMENT '最大rank',
    `max_level` int(11) DEFAULT NULL COMMENT '最大等级',
    `is_love_max` tinyint DEFAULT NULL COMMENT '是否满绊',
    `is_removable_skill_capacity_max` tinyint DEFAULT NULL COMMENT '是否最大宝石容量',
    `rank` int(11) DEFAULT NULL COMMENT '当前rank',
    `next_exp` int(11) DEFAULT NULL COMMENT '下一级exp',
    `exp` int(11) DEFAULT NULL COMMENT '当前exp',
    `unit_id` int(11) DEFAULT NULL COMMENT '成员ID',
    `favorite_flag` tinyint DEFAULT NULL COMMENT '是否锁定',
    `is_rank_max` tinyint DEFAULT NULL COMMENT '是否最大rank',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='成员信息';

CREATE TABLE `secret_box` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
    `user_id` int(11) NOT NULL COMMENT '用户id',
    `secret_box_id` int(11) NOT NULL COMMENT '招募ID',
    `name` varchar(256) DEFAULT NULL COMMENT '招募名称',
    `unit_id` int(11) DEFAULT NULL COMMENT '成员ID',
    `rank` int(11) DEFAULT NULL COMMENT '当前rank',
    `rarity` int(11) DEFAULT NULL COMMENT '成员稀有度',
    `pon_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='招募信息';
