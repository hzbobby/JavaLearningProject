package com.bobby.localmessage.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // 必须设置为RUNTIME，否则反射无法获取
@Target(ElementType.METHOD) // 限定注解只能用在方法上
public @interface LocalMessage {
    // 可以添加一些自定义属性
    String value() default "";

    /**
     * 最大重试次数 （包括第一次正常执行）
     * @return
     */
    int maxRetryTimes() default 3;

    /**
     * 是否异步执行
     * @return
     */
    boolean async() default true;
}