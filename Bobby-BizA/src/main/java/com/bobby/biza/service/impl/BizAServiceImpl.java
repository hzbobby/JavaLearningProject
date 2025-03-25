package com.bobby.biza.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bobby.biza.domain.BizA;
import com.bobby.biza.domain.LocalMessage;
import com.bobby.biza.mapper.BizAMapper;
import com.bobby.biza.service.IBizAService;
import com.bobby.biza.service.ILocalMessageService;
import com.bobby.common.utils.MqConstants;
import com.bobby.common.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Service
public class BizAServiceImpl extends ServiceImpl<BizAMapper, BizA> implements IBizAService {

    private final RestTemplate restTemplate;

    private final RabbitTemplate rabbitTemplate;


    // 同步调用过程，并添加上事务

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result doBizA() {
      boolean res = doBizAMQLocalMsg();
//      boolean res = doBizASync();
//        boolean res = doBizAMQ();
        if(!res){
            return Result.fail("失败");
        }
        return Result.ok();
    }

    // 同步调用，利用 HTTP / RPC 进行同步调用
    private Boolean doBizASync() {
        log.debug("开始执行 BizA");
        // 现在我们要在 A 里面调用 远程服务 B 的事务
        // doBiz in A
        // 假设我们现在的业务就是 new BizA 然后存到数据库
        BizA bizA = new BizA();
        bizA.setName("BizA_" + RandomUtil.randomString(6));
        bizA.setValue(Integer.valueOf(RandomUtil.randomNumbers(3)));
        boolean bizASuccess = save(bizA);
        if (!bizASuccess) {
            log.error("执行 bizA 失败");
            throw new RuntimeException("执行 bizA 失败");
        }
        log.debug("保存 bizA 成功");

        log.debug("远程调用 BizB");
        // doBiz in B
        // 进行远程调用
        // 1. RestTemplate
        ResponseEntity<Result> response = restTemplate.exchange(
                "http://localhost:8082/biz/do/{id}",
                HttpMethod.POST,
                new HttpEntity<>(null),  // 或者传入合适的请求体
                Result.class,
                bizA.getId()  // 直接传递值，或者使用 Map.of("id", bizA.getId())
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("远程调用 BizB 失败");
            throw new RuntimeException("远程调用 BizB 失败");
        }
        log.debug("远程调用 BizB 成功");
        Result result = response.getBody();
        if (!result.getSuccess()) {
            log.error("执行 bizB 失败");
            throw new RuntimeException("执行 bizB 失败");
        }
        log.debug("执行 bizB 成功");
        log.debug("执行 bizA 成功");
        return true;
    }

    // 同步调用，利用 MQ 进行解耦
    // 如何保证消息的事务性呢
    private Boolean doBizAMQ() {
        log.debug("开始执行 BizA");
        // 现在我们要在 A 里面调用 远程服务 B 的事务
        // doBiz in A
        // 假设我们现在的业务就是 new BizA 然后存到数据库
        BizA bizA = new BizA();
        bizA.setName("BizA_" + RandomUtil.randomString(6));
        bizA.setValue(Integer.valueOf(RandomUtil.randomNumbers(3)));
        boolean bizASuccess = save(bizA);
        if (!bizASuccess) {
            log.error("执行 bizA 失败");
            throw new RuntimeException("执行 bizA 失败");
        }
        log.debug("保存 bizA 成功");

        log.debug("发送消息到 BizB");
        // 给生产者添加确认回调
        sendMsg(bizA.getId());
        // 到这里我们不知道 BizB 执行成功与否
        // 我们只知道 BizA 执行是成功的
        // 并且这里无法保证 BizA 与 BizA 的执行事务性
        // 事务的最终一致性，需要靠 MQ的可靠性 来完成
        log.debug("执行 bizA 成功");
        return true;
    }

    public void sendMsg(Long id){
        log.debug("发送消息到 BizB");
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
            rabbitTemplate.convertAndSend(MqConstants.EXCHANGE_BIZ_DIRECT, MqConstants.KEY_SAVE_BIZB, String.valueOf(id), cd);
        } catch (Exception e) {
            log.error("消息发送异常: " + e.getMessage());
        }
    }

    private final ILocalMessageService localMessageService;

    private Boolean doBizAMQLocalMsg() {
        log.debug("开始执行 BizA");
        // 现在我们要在 A 里面调用 远程服务 B 的事务
        // doBiz in A
        // 假设我们现在的业务就是 new BizA 然后存到数据库
        BizA bizA = new BizA();
        bizA.setName("BizA_" + RandomUtil.randomString(6));
        bizA.setValue(Integer.valueOf(RandomUtil.randomNumbers(3)));
        boolean bizASuccess = save(bizA);
        if (!bizASuccess) {
            log.error("执行 bizA 失败");
            throw new RuntimeException("执行 bizA 失败");
        }
        log.debug("保存 bizA 成功");
        // 保存本地消息
        LocalMessage localMessage = new LocalMessage();
        localMessage.setContent(String.valueOf(bizA.getId()));
        localMessage.setStatus(0); // 消息发送中
        boolean localMsgSuccess = localMessageService.save(localMessage);
        if (!localMsgSuccess) {
            log.error("保存 local msg 失败");
            throw new RuntimeException("保存 local msg 失败");
        }
        // 发送消息到 mq
        sendMsg(bizA.getId());
        // 这里将 保存 bizA 与 保存本地消息放在同一个事务中，保证同时成功或同时失败。
        log.debug("执行 bizA 成功");
        return true;
    }


}
