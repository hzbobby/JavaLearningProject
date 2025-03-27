package com.bobby.biza.schedule;

import com.bobby.biza.domain.LocalMessage;
import com.bobby.biza.domain.LocalMessageDO;
import com.bobby.biza.domain.enums.MsgStatus;
import com.bobby.biza.service.IBizAService;
import com.bobby.biza.service.ILocalMessageDOService;
import com.bobby.biza.service.ILocalMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Component
public class ScheduledTasks {
    private final ILocalMessageService localMessageService;
    private final ILocalMessageDOService localMessageDOService;
    private final IBizAService bizaService;
    /**
     * 定时扫描本地消息表
     * 例如，5s 扫描一次
     */
//    @Scheduled(fixedRate = 5000)
    public void scanningUnConfirmLocalMessage() {
        log.debug("扫描本地消息表");
        List<LocalMessage> list = localMessageService.lambdaQuery().eq(LocalMessage::getStatus, 0).list();
        log.debug("未确认消息数量: {}", list.size());
        list.forEach(localMessage -> {
            bizaService.sendMsg(Long.parseLong(localMessage.getContent()));
        });
    }


    // 仍然是利用一个 schedule 去捞本地消息表没成功的消息
    @Scheduled(fixedRate = 5000)
    public void retryLocalMsg() {
        log.debug("扫描未成功的消息");
        List<LocalMessageDO> list = localMessageDOService.lambdaQuery().ne(LocalMessageDO::getStatus, MsgStatus.SUCCESS.getStatus()).list();
        list.forEach(localMessage -> {
            // 这里获取的是代理类，因此 sendMsg 是可以走代理的
            // 检查是否到达重试时间，检查是否还能重试

            if(localMessage.getNextRetryTime() < System.currentTimeMillis()) {
                // 重试次数 + 1
                if(localMessage.getRetryTimes() < localMessage.getMaxRetryTimes()) {
                    // 可以继续重试
                    localMessage.setRetryTimes(localMessage.getRetryTimes() + 1);
                    localMessageDOService.updateById(localMessage);
                    bizaService.sendMsg(localMessage.getId());
                }else{
                    log.debug("消息 {} 超出最大重试次数，已停止重试。", localMessage.getId());
                    //TODO 超过最大重试次数的消息
                    // 可以投递到特定的库中
                    // 这里调用告警，通知人工介入
                }
            }

        });
    }

}
