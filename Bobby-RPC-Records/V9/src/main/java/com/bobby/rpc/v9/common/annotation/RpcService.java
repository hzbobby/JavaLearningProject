package com.bobby.rpc.v9.common.annotation;

import java.lang.annotation.*;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/3/30
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RpcService {
    /**
     * 服务接口类
     * @return 接口Class对象
     */
    Class<?> interfaceClass() default void.class;

    boolean retryable() default false;

    String version() default "0.01";
}