package com.bobby.aspect.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

@Aspect
public class ShowAspect {
    // 在里面定义 切点 和 切面操作
    @Pointcut("execution(* com.bobby.aspect.shows.*.perform(..))")
    public void perform() {
    }

    @Before("perform()")
    public void performBefore() {
        System.out.println("Before perform");
    }

    @After("perform()")
    public void performAfter() {
        System.out.println("After perform");
    }

    @AfterReturning("perform()")
    public void performAfterReturning() {
        System.out.println("After perform returning");
    }

    @Around("perform()")
    public Object performAround(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("Around before");
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        System.out.println(method.getName());
        pjp.proceed();
        System.out.println("Around after");
        return null;
    }

}
