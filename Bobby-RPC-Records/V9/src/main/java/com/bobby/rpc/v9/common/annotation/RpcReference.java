package com.bobby.rpc.v9.common.annotation;

import java.lang.annotation.*;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/3/30
 *
 * 本质上是通过这个注解，扫描到需要注入的位置
 * 然后对该位置的接口进行代理
 * 代理类做的事情就是
 * - 构建请求
 * - 拿到数据
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface RpcReference {
    Class<?> interfaceClass() default void.class;

    String version() default "0.01";
}
