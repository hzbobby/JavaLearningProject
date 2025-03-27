package com.bobby.biza.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bobby.biza.aspect.InvokeCtx;
import com.bobby.biza.aspect.InvokeStatusHolder;
import com.bobby.biza.domain.LocalMessageDO;
import com.bobby.biza.domain.enums.MsgStatus;
import com.bobby.biza.mapper.LocalMessageDOMapper;
import com.bobby.biza.service.ILocalMessageDOService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class LocalMessageDOServiceImpl extends ServiceImpl<LocalMessageDOMapper, LocalMessageDO> implements ILocalMessageDOService {

    private final ApplicationContext applicationContext;
    private final LocalMessageDOMapper localMessageDOMapper;

    @Override
    public void invoke(LocalMessageDO localMessageDO, boolean async) {
        // 1. 消息幂等性检查


        // 这里先对消息进行保存
        save(localMessageDO);
        // 如果在事务中的话，上述操作也是在一个事务中的
        boolean inTx = TransactionSynchronizationManager.isActualTransactionActive();
        if (inTx) {
            // 如果在事务中，则在事务完成后，再执行
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
                        execute(localMessageDO, async);
                    }
                }
            });
        } else {
            // 普通执行
            execute(localMessageDO, async);
        }

    }

    @Override
    public void increaseInvokeTimes(Long id) {
        localMessageDOMapper.increateInvokeTimes(id);
    }


    private void execute(LocalMessageDO localMessageDO, boolean async) {
        if (async) {
            doAsyncInvoke(localMessageDO);
        } else {
            doInvoke(localMessageDO);
        }
    }

    private void doInvoke(LocalMessageDO localMessageDO) {
        // 通过反射获取消息的方法，然后调用

        String reqSnapshot = localMessageDO.getReqSnapshot();
        if (StrUtil.isBlank(reqSnapshot)) {
            log.warn("Request snapshot is empty, record: {}", localMessageDO.getId());
            invokeFail(localMessageDO, "Request snapshot is empty.");
            return;
        }
        // parseObject 有 null
        InvokeCtx ctx = JSON.parseObject(reqSnapshot, InvokeCtx.class);

        // 在这里进行反射调用
        try {
            InvokeStatusHolder.startInvoke();
            Class<?> target = Class.forName(ctx.getClassName());
            Object bean = applicationContext.getBean(target);

            List<Class<?>> paramTypes = getParamTypes(JSON.parseArray(ctx.getParamTypesJson(), String.class));
            Method method = target.getMethod(ctx.getMethodName(), paramTypes.toArray(new Class[0]));
            Object[] args = getArgs(paramTypes, ctx.getArgsJson());
            // 参数类型可能不正确
            method.invoke(bean, args);
            // 更新本地消息表的状态
            invokeSuccess(localMessageDO);
        } catch (ClassNotFoundException e) {
            invokeFail(localMessageDO, "ClassNotFoundException: "+e.getMessage());
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            invokeFail(localMessageDO, "NoSuchMethodException: "+e.getMessage());

            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            invokeFail(localMessageDO, "InvocationTargetException: "+e.getMessage());

            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            invokeFail(localMessageDO, "IllegalAccessException: "+e.getMessage());

            throw new RuntimeException(e);
        }finally {
            InvokeStatusHolder.endInvoke();
        }
    }

    private void invokeSuccess(LocalMessageDO localMessageDO) {
        localMessageDO.setStatus(MsgStatus.SUCCESS.getStatus());
        lambdaUpdate().update(localMessageDO);
//        lambdaUpdate().eq(LocalMessageDO::getId, localMessageDO.getId()).update();
    }

    private void invokeFail(LocalMessageDO localMessageDO, String failMsg) {
        localMessageDO.setStatus(MsgStatus.FAIL.getStatus());
        localMessageDO.setFailReason(failMsg);
        lambdaUpdate().update(localMessageDO);
    }

    private void doAsyncInvoke(LocalMessageDO localMessageDO) {
        // 使用异步线程开启调用

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
                throw new IllegalArgumentException("第" + (i+1) + "个参数转换失败", e);
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
            case "byte": return byte.class;
            case "short": return short.class;
            case "int": return int.class;
            case "long": return long.class;
            case "float": return float.class;
            case "double": return double.class;
            case "char": return char.class;
            case "boolean": return boolean.class;
            case "void": return void.class;
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
