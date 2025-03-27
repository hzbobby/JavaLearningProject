package com.bobby.biza.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bobby.biza.domain.LocalMessageDO;
import org.apache.ibatis.annotations.Select;

public interface LocalMessageDOMapper extends BaseMapper<LocalMessageDO> {

    @Select("select status from local_message_aop where id = ${msgId}")
    public String getStatus(Long msgId);

    @Select("update for local_message_aop set retryTimes = retryTimes + 1 where id = ${msgId}")
    boolean increateInvokeTimes(Long msgId);
}
