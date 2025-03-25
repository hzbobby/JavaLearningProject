package com.bobby.biza.listener;


import com.bobby.biza.domain.LocalMessage;
import com.bobby.biza.service.ILocalMessageService;
import com.bobby.common.utils.MqConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class MsgConfirmListener {

    private final ILocalMessageService localMessageService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = MqConstants.QUEUE_BIZA_LOCAL_MSG, durable = "true"),
                    exchange = @Exchange(name = MqConstants.EXCHANGE_BIZ_DIRECT, durable = "true"),
                    key = {
                            MqConstants.KEY_MSG_CONFIRM
                    }
            )
    )
    public void msgConfirm(Long bizId){
        log.debug("LocalMsg 监听到消息: "+bizId);
        // 这里根据 biz_id 进行修改
        LocalMessage localMessage = new LocalMessage();
        localMessage.setId(bizId);
        localMessage.setStatus(1);
        localMessageService.lambdaUpdate().eq(LocalMessage::getContent, String.valueOf(bizId))
                .set(LocalMessage::getStatus, 1)
                .update();
    }
}
