CREATE TABLE `local_message` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` tinyint NOT NULL DEFAULT 0,
    `req_snapshot` TEXT NOT NULL COMMENT '请求快照参数json',
    `status` int NOT NULL DEFAULT 0 COMMENT '状态 INIT, FAIL, SUCCESS',
    `next_retry_time` BIGINT NOT NULL COMMENT '下一次重试的时间',
    `retry_times` int NOT NULL DEFAULT 0 COMMENT '已经重试的次数',
    `max_retry_times` int NOT NULL COMMENT '最大重试次数',
    `fail_reason` text COMMENT '执行失败的信息',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='本地消息表';