CREATE TABLE `biz_b` (
                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                         `name` varchar(255) DEFAULT NULL COMMENT '名称',
                         `value` int DEFAULT NULL COMMENT '值',
                         `a_id` bigint DEFAULT NULL COMMENT '关联biz_a表的ID',
                         `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         PRIMARY KEY (`id`),
                         KEY `idx_a_id` (`a_id`) COMMENT '关联biz_a表的索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务表B';