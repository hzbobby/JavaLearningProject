package com.bobby.bizb.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bobby.bizb.domain.BizB;
import com.bobby.bizb.mapper.BizBMapper;
import com.bobby.bizb.service.IBizBService;
import com.bobby.common.utils.MqConstants;
import com.bobby.common.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Slf4j
@Service
public class BizBServiceImpl extends ServiceImpl<BizBMapper, BizB> implements IBizBService {
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result doBizB(Long id) {
        // 在这里我们将调用 BizB 的服务
        log.debug("开始执行 bizB");
        // 1. 进行幂等性校验
        if (idempotentCheck(id)) {
            // 直接返回
            // 可能是 bizA 没收到回复消息。
            // 重新发送回复消息
            log.debug("幂等性校验：重复提交，id: " + id);
            return Result.ok("重复提交");
        }

        BizB bizB = new BizB();
        bizB.setAId(id);
        bizB.setName("BizA_" + RandomUtil.randomString(6));
        bizB.setValue(Integer.valueOf(RandomUtil.randomNumbers(3)));
        boolean bizBSuccess = save(bizB);
        try {
            log.debug("休眠 20 ms 模拟耗时操作");
            Thread.sleep(20);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (!bizBSuccess) {
            log.error("执行 bizB 失败");
            throw new RuntimeException("执行 bizB 失败");
        }
        log.debug("保存 bizB 成功");
        return Result.ok();
    }


    private boolean idempotentCheck(Long id) {
        // 检查是否由是重复提交的
        // 这里利用业务进行幂等性校验。
        BizB one = lambdaQuery().eq(BizB::getAId, id).one();
        return one != null;
    }

    private final RabbitTemplate rabbitTemplate;

    public void sendReplyMsg(Long id) {
        log.debug("发送消息到 BizA");
        // 给生产者添加确认回调
        try {
            CorrelationData cd = new CorrelationData();
            cd.getFuture().thenAccept(confirm -> {
                if (!confirm.isAck()) {
                    log.error("消息未确认: " + confirm.getReason());
                } else {
                    log.debug("消息确认成功，消息ID: " + cd.getId());
                }
            });
            rabbitTemplate.convertAndSend(MqConstants.EXCHANGE_BIZ_DIRECT, MqConstants.KEY_MSG_CONFIRM, id, cd);
        } catch (Exception e) {
            log.error("消息发送异常: " + e.getMessage());
        }
    }


}
