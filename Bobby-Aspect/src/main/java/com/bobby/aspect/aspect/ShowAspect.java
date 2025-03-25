package com.bobby.aspect.aspect;

import org.aspectj.lang.annotation.*;

@Aspect
public class ShowAspect {
    // 在里面定义 切点 和 切面操作
    @Pointcut("execution(* com.bobby.aspect.shows.*.perform(..))")
    public void perform() {}

    @Before("perform()")
    public void performBefore(){
        System.out.println("Before perform");
    }

    @After("perform()")
    public void performAfter(){
        System.out.println("After perform");
    }

    @AfterReturning("perform()")
    public void performAfterReturning(){
        System.out.println("After perform returning");
    }
}
