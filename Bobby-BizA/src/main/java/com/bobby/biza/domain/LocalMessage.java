package com.bobby.biza.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("local_message")  // 指定对应的数据库表名
public class LocalMessage {

    /**
     * 主键ID，自增长
     */
    @TableId(type = IdType.AUTO)  // 自增主键
    private Long id;

    /**
     * 业务ID
     */
    @TableField("content")  // 可省略，名称一致时MyBatis-Plus会自动映射
    private String content;

    /**
     * 消息状态
     * 0-发送中, 1-已完成
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)  // 自动填充策略
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)  // 自动填充策略
    private LocalDateTime updateTime;
}