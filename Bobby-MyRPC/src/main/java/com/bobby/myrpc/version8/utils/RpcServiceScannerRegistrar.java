package com.bobby.myrpc.version8.utils;

import com.bobby.myrpc.version8.annotation.RpcService;
import com.bobby.myrpc.version8.annotation.ServiceScan;
import com.bobby.myrpc.version8.register.ServiceCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Map;

@Slf4j
public class RpcServiceScannerRegistrar implements ImportBeanDefinitionRegistrar {
    private final ServiceCollector serviceCollector;

    public RpcServiceScannerRegistrar(ServiceCollector serviceCollector) {
        this.serviceCollector = serviceCollector;
    }

    @Override
    public void registerBeanDefinitions(
            AnnotationMetadata importingClassMetadata,
            BeanDefinitionRegistry registry) {
        log.info("Registering RpcServiceScannerRegistrar");
        // 向 Spring 容器中注册 bean 实例

        //      先获取所有该注解的 bean
        //      然后再通过包过滤

        // 1. 获取 @ServiceScan 注解的 basePackages 配置
        Map<String, Object> annotationAttributes = importingClassMetadata
                .getAnnotationAttributes(ServiceScan.class.getName());
        String[] basePackages = (String[]) annotationAttributes.get("basePackages");

        // 2. 如果没有配置包路径，则使用启动类所在包
        if (basePackages.length == 0) {
            basePackages = new String[]{
                    ((StandardAnnotationMetadata) importingClassMetadata)
                            .getIntrospectedClass().getPackage().getName()
            };
        }

        // 3. 创建自定义扫描器
        RpcServiceScanner scanner = new RpcServiceScanner(registry, serviceCollector);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RpcService.class));
        scanner.scan(basePackages);
    }
}