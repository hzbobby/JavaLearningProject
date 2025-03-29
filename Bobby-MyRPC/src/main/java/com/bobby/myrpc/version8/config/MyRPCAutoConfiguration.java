package com.bobby.myrpc.version8.config;

import com.bobby.myrpc.version8.annotation.RpcService;
import com.bobby.myrpc.version8.register.IServiceRegister;
import com.bobby.myrpc.version8.register.ServiceCollector;
import com.bobby.myrpc.version8.register.ZkServiceRegister;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Slf4j
@Configuration
@EnableConfigurationProperties({MyRPCProperties.class, ZkProperties.class,}) // 启用配置绑定
public class MyRPCAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "myrpc", name = "enabled", havingValue = "true", matchIfMissing = true)
    public IServiceRegister zkServiceRegister(
            MyRPCProperties appProperties,
            ZkProperties zkProperties, ApplicationContext applicationContext) {
        ZkServiceRegister register = new ZkServiceRegister(appProperties.getName(), zkProperties);

        String[] beanNamesForAnnotation = applicationContext.getBeanNamesForAnnotation(RpcService.class);
        log.info("扫描到服务类beans: " + Arrays.toString(beanNamesForAnnotation));

        return register;
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceCollector serviceCollector() {
        return new ServiceCollector();
    }


}