package com.bobby.bizb.listener;


import com.bobby.bizb.service.IBizBService;
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
public class SaveBizListener {
    private final IBizBService bizBService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = MqConstants.QUEUE_BIZB, durable = "true"),
                    exchange = @Exchange(name = MqConstants.EXCHANGE_BIZ_DIRECT, durable = "true"),
                    key = {
                            MqConstants.KEY_SAVE_BIZB
                    }
            )
    )
    public void saveBizB(Long id) {
        log.debug("BizB 监听到消息: " + id);
        bizBService.doBizB(id);
        // 消费完成后发送 状态变更消息
        bizBService.sendReplyMsg(id);
    }
}
