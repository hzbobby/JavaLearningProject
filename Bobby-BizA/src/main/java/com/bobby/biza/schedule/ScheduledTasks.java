package com.bobby.biza.schedule;

import com.bobby.biza.domain.LocalMessage;
import com.bobby.biza.service.IBizAService;
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
    private final IBizAService bizaService;
    /**
     * 定时扫描本地消息表
     * 例如，5s 扫描一次
     */
    @Scheduled(fixedRate = 5000)
    public void scanningUnConfirmLocalMessage() {
        log.debug("扫描本地消息表");
        List<LocalMessage> list = localMessageService.lambdaQuery().eq(LocalMessage::getStatus, 0).list();
        log.debug("未确认消息数量: {}", list.size());
        list.forEach(localMessage -> {
            bizaService.sendMsg(Long.parseLong(localMessage.getContent()));
        });
    }
}
