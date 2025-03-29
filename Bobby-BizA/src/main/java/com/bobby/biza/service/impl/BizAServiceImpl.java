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
import org.springframework.context.ApplicationContext;
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
    private final ApplicationContext applicationContext;





    // 同步调用过程，并添加上事务

//    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result doBizA() {
//      boolean res = doBizAMQLocalMsg();
      boolean res = doBizASync();
//        boolean res = doBizAMQ();
//        boolean res = doBizAMQAop();
        if (!res) {
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

    private Boolean doBizASyncMyRPC() {
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

    @com.bobby.biza.aspect.LocalMessage(maxRetryTimes = 3, async = false)
    public void sendMsg(Long id) {
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

    /// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 上面的例子中，我们是将本地消息表的操作与业务操作耦合了。
    // 下面假设，我们还没有使用本地消息表，但是我们用了 MQ 来解耦三方调用
    // 为了保证三方调用 与 本地业务的事务性
    // 我们需要保证 三方调用 执行成功
    private Boolean doBizAMQAop() {
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
        // 发送消息到 mq
        // 这里相当于是三方调用，三方调用一般是不需要返回数据的
        // 因此，可以将这层调用剥离出来。
        // 我们把这层调用的逻辑，存为本地消息
        // 要针对本地消息的内容发起调用，我们可以通过反射，因此就需要有例如类名，方法名，参数等信息
        // 这里得拿到 AOP 的代理，不然调不到 AOP 切面
        sendMsgProxy(bizA.getId());
        // 我们把三方调用执行的逻辑抽离出来
        // 通过 Aop 可以在三方调用执行前，将消息写入本地
        // 如何保证消息的写入与本地业务是在同一个事务呢？
        // 我们的切面可以知道，当前这个三方调用是在一个事务里面吗？

        log.debug("执行 bizA 成功");
        return true;
    }

    private void sendMsgProxy(Long id){
        applicationContext.getBean(IBizAService.class).sendMsg(id);
    }

}
