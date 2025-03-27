package com.bobby.localmessage.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bobby.localmessage.aspect.InvokeCtx;
import com.bobby.localmessage.aspect.InvokeStatusHolder;
import com.bobby.localmessage.domain.LocalMessageDO;
import com.bobby.localmessage.domain.enums.MsgStatus;
import com.bobby.localmessage.mapper.LocalMessageDOMapper;
import com.bobby.localmessage.service.ILocalMessageDOService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class LocalMessageDOServiceImpl extends ServiceImpl<LocalMessageDOMapper, LocalMessageDO> implements ILocalMessageDOService {

    private final ApplicationContext applicationContext;

    // 线程池
    private final ExecutorService localMessageExecutor = Executors.newFixedThreadPool(10);

    // 仍然是利用一个 schedule 去捞本地消息表没成功的消息
    @Scheduled(fixedRate = 5000)
    public void retryLocalMsg() {
        log.debug("retryLocalMsg");
        loadWaitRetryRecords().forEach(this::doAsyncInvoke);
    }

    @Override
    public void invoke(LocalMessageDO localMessageDO, boolean async) {
        // 1. 消息幂等性检查
        //  防止重试的消息，再次保存进数据库
        // TODO idempontant
        // 2. 把消息持久化。如果开启了事务，这里是与本地业务在同一个事务内的。保证了本地业务与保存消息的原子性
        save(localMessageDO);

        boolean inTx = TransactionSynchronizationManager.isActualTransactionActive();
        if (inTx) {
            // 3. 如果在事务中，则在事务提交后，再执行
            // 这里用到了事务的钩子方法，Spring 提供了这样的钩子方便在事务提交后进行其他处理
            // 这里我们注册一个 TransactionSynchronization 接口
            // 它允许我们在事务的生命周期中进行回调
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public int getOrder() {
                    return TransactionSynchronization.super.getOrder();
                }

                @Override
                public void afterCompletion(int status) {
                    if (status == TransactionSynchronization.STATUS_COMMITTED) {
                        // 事务完成的时候做以下处理
                        log.debug("afterCompletion committed, 开始执行三方调用");
                        // 4. 执行三方调用逻辑
                        execute(localMessageDO, async);
                    }
                }
            });
        } else {
            // 4. 执行三方调用逻辑
            execute(localMessageDO, async);
        }

    }

    @Override
    public List<LocalMessageDO> loadWaitRetryRecords() {
        // 读取本地消息表中
        // 1. 超时的消息
        // 2. 没执行成功的消息
        return lambdaQuery()
                .le(LocalMessageDO::getNextRetryTime, System.currentTimeMillis())
                .eq(LocalMessageDO::getStatus, MsgStatus.RETRY.getStatus())
                .list();
    }

    private void execute(LocalMessageDO localMessageDO, boolean async) {
        if (async) {
            doAsyncInvoke(localMessageDO);
        } else {
            doInvoke(localMessageDO);
        }
    }

    private void doInvoke(LocalMessageDO localMessageDO) {
        // 1. 获取三方调用方法的信息，进行反射，构造出该方法
        String reqSnapshot = localMessageDO.getReqSnapshot();
        if (StrUtil.isBlank(reqSnapshot)) {
            log.warn("Request snapshot is empty, record: {}", localMessageDO.getId());
            invokeFail(localMessageDO, "Request snapshot is empty.");
            return;
        }
        // 2. 构造调用信息
        InvokeCtx ctx = JSON.parseObject(reqSnapshot, InvokeCtx.class);

        // 3. 在这里进行反射调用
        try {
            // 4. 记录一下调用状态，方便快速失败，防止多次调用
            InvokeStatusHolder.startInvoke();
            // 5. 下面就是反射获取原方法了。注意，这里三方调用是获取了该方法的代理对象 bean
            Class<?> target = Class.forName(ctx.getClassName());
            Object bean = applicationContext.getBean(target);

            List<Class<?>> paramTypes = getParamTypes(JSON.parseArray(ctx.getParamTypesJson(), String.class));
            Method method = target.getMethod(ctx.getMethodName(), paramTypes.toArray(new Class[0]));
            Object[] args = getArgs(paramTypes, ctx.getArgsJson());

            method.invoke(bean, args);
            // 6. 更新本地消息表的状态
            invokeSuccess(localMessageDO);
        } catch (ClassNotFoundException e) {
            // 7. 更新失败信息
            invokeFail(localMessageDO, "ClassNotFoundException: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            invokeFail(localMessageDO, "NoSuchMethodException: " + e.getMessage());

            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            invokeFail(localMessageDO, "InvocationTargetException: " + e.getMessage());

            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            invokeFail(localMessageDO, "IllegalAccessException: " + e.getMessage());

            throw new RuntimeException(e);
        } finally {
            // 8. 结束调用
            InvokeStatusHolder.endInvoke();
        }
    }

    private void invokeSuccess(LocalMessageDO localMessageDO) {
        localMessageDO.setStatus(MsgStatus.SUCCESS.getStatus());
        lambdaUpdate().update(localMessageDO);
    }

    private void invokeFail(LocalMessageDO localMessageDO, String failMsg) {
        retry(localMessageDO, failMsg);
    }

    private void retry(LocalMessageDO localMessageDO, String faiMsg) {
        Integer retryTimes = localMessageDO.getRetryTimes() + 1;
        localMessageDO.setFailReason(faiMsg);
        if (retryTimes < localMessageDO.getMaxRetryTimes()) {
            // 处于 retry 可以被定时任务捞起来
            localMessageDO.setRetryTimes(retryTimes);
            localMessageDO.setStatus(MsgStatus.RETRY.getStatus());
        } else {
            // 处于 fail 的需要人工去处理了
            localMessageDO.setStatus(MsgStatus.FAIL.getStatus());
        }
        lambdaUpdate().update(localMessageDO);
    }

    private void doAsyncInvoke(LocalMessageDO localMessageDO) {
        // 使用异步线程开启调用
        localMessageExecutor.execute(() -> doInvoke(localMessageDO));
    }

    private Object[] getArgs(List<Class<?>> paramTypes, String argsJson) {
        // 1. 解析JSON数组
        List<Object> jsonArgs = JSON.parseArray(argsJson);

        // 2. 检查参数数量是否匹配
        if (jsonArgs.size() != paramTypes.size()) {
            throw new IllegalArgumentException("参数数量不匹配");
        }

        // 3. 转换每个参数
        Object[] args = new Object[paramTypes.size()];
        for (int i = 0; i < args.length; i++) {
            try {
                args[i] = JSON.parseObject(JSON.toJSONString(jsonArgs.get(i)), paramTypes.get(i));
            } catch (Exception e) {
                throw new IllegalArgumentException("第" + (i + 1) + "个参数转换失败", e);
            }
        }

        return args;
    }

    private List<Class<?>> getParamTypes(List<String> paramTypes) {
        return paramTypes.stream()
                .map(this::resolveClass) // 解析每个类名
                .collect(Collectors.toList());
    }

    private Class<?> resolveClass(String className) {
        // 处理基本类型
        switch (className) {
            case "byte":
                return byte.class;
            case "short":
                return short.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            case "char":
                return char.class;
            case "boolean":
                return boolean.class;
            case "void":
                return void.class;
        }

        // 处理数组类型（如"[Ljava.lang.String;"）
        if (className.startsWith("[")) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Invalid array type: " + className, e);
            }
        }

        // 处理普通引用类型
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class not found: " + className, e);
        }
    }
}
