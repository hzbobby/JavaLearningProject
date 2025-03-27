package com.bobby.biza.aspect;

import com.alibaba.fastjson2.JSON;
import com.bobby.biza.domain.LocalMessageDO;
import com.bobby.biza.service.ILocalMessageDOService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;

@RequiredArgsConstructor
@Slf4j
@Aspect
@Component
public class LocalMessageAspect {
    private final long RETRY_INTERVAL_MILLSECONDS = 1L * 60 * 1000;

    private final ILocalMessageDOService localMessageDOService;

    // 定义切点
    @Pointcut("@annotation(com.bobby.biza.aspect.LocalMessage)")
    public void localMessageMethod() {
    }

    // 定义切面
    // 在方法执行前，先进行消息的保存，保证消息持久化到本地
    // 这里使用 Around 通知，可以在三方方法执行前后做些处理
    @Around("localMessageMethod()")
    public Object doAspect(ProceedingJoinPoint pjp) throws Throwable {
        // 通过反射可以拿到三方方法的类名，方法名，参数
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        // 拿到方法的注解
        LocalMessage annotation = method.getAnnotation(LocalMessage.class);
        if (Objects.isNull(annotation)) {
            // 如果没使用该注解，按照正常方法执行
            return pjp.proceed();
        }
        // 如果方法正在执行
        // 因为是Around 需要对执行状态增加一个判断
        if (InvokeStatusHolder.inInvoke()) {
            log.debug("进入 invoke");
            return pjp.proceed();
        }

        boolean async = annotation.async();
        // 获取方法参数，转换成对应参数类型
        // steam 操作方便一点
//        List<String> params = Arrays.stream(method.getParameterTypes())
//                .map(Class::getName)
//                .toList();
        Class<?>[] parameterTypes = method.getParameterTypes();
        // 记录执行上下文
        // 获取类名，方法名，参数类型，参数等
        // 这里应用建造者模式，来组装参数
        InvokeCtx ctx = InvokeCtx.builder()
                .className(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .paramTypes(JSON.toJSONString(parameterTypes))
                .args(JSON.toJSONString(pjp.getArgs()))
                .build();
        LocalMessageDO localMessageDO = LocalMessageDO.of(JSON.toJSONString(ctx), annotation.maxRetryTimes(), offsetTimestamp(RETRY_INTERVAL_MILLSECONDS));
        log.debug("record local message, record:{}, async:{}", JSON.toJSONString(ctx), async);
        // 组装完参数后，这个就是一个可以完整执行调用的消息了

        // 这里调用执行一次本地消息

        localMessageDOService.invoke(localMessageDO, async);
        return null;
    }

    private long offsetTimestamp(long timeLength) {
        return System.currentTimeMillis() + timeLength;
    }

}
