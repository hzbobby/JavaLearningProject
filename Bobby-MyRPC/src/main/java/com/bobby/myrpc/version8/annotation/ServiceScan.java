package com.bobby.myrpc.version8.annotation;

import com.bobby.myrpc.version8.utils.RpcServiceScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 扫描指定包路径下的 @RpcService 注解类
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(RpcServiceScannerRegistrar.class) // 关联扫描器，在 spring 启动时，执行扫描逻辑
public @interface ServiceScan {
    /**
     * 要扫描的包路径（支持多个）
     */
    String[] basePackages() default {};
}