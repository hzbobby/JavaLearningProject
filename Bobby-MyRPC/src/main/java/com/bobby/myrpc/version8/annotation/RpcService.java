package com.bobby.myrpc.version8.annotation;

import java.lang.annotation.*;

/**
 * RPC服务暴露注解
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

    /**
     * 服务接口全限定名
     * @return 接口完整类名（当interfaceClass未指定时使用）
     */
    String interfaceName() default "";

}