package com.bobby.rpc.v9.starter.common.annotation;

import com.bobby.rpc.v9.starter.config.BRpcAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/4/6
 * 通过该注解，将我们的 RPC 框架引入到项目中
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(value = {BRpcAutoConfiguration.class})
public @interface EnableBRpc {
}
