package com.bobby.biza.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bobby.biza.domain.enums.MsgStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("local_message_aop")
public class LocalMessageDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("deleted")
    private Integer deleted;

    @TableField("req_snapshot")
    private String reqSnapshot;

    @TableField("status")
    private Integer status;

    @TableField("next_retry_time")
    private Long nextRetryTime;

    @TableField("retry_times")
    private Integer retryTimes;

    @TableField("max_retry_times")
    private Integer maxRetryTimes;

    @TableField("fail_reason")
    private String failReason;

    public static LocalMessageDO of(String ctxJson, int maxRetryTimes, long nextRetryTime) {
        LocalMessageDO localMessageDO = new LocalMessageDO();
        localMessageDO.reqSnapshot = ctxJson;
        localMessageDO.retryTimes = 1;
        localMessageDO.maxRetryTimes = maxRetryTimes;
        localMessageDO.nextRetryTime = nextRetryTime;
        localMessageDO.status = MsgStatus.SENDING.getStatus();
        return localMessageDO;
    }
}