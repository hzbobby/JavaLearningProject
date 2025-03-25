CREATE TABLE local_message (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID，自增长',
                               content VARCHAR(64) NOT NULL COMMENT '业务ID',
                               status TINYINT NOT NULL DEFAULT 0 COMMENT '消息状态，0-发送中, 1-已完成',
                               create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                               INDEX idx_status (status) COMMENT '状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='本地消息表';