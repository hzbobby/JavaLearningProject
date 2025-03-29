package com.bobby.myrpc.version8.utils;

import com.bobby.myrpc.version8.annotation.RpcService;
import com.bobby.myrpc.version8.register.ServiceCollector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;

import java.util.Set;

@Slf4j
public class RpcServiceScanner extends ClassPathBeanDefinitionScanner {
    private final ServiceCollector collector;

    public RpcServiceScanner(BeanDefinitionRegistry registry, ServiceCollector collector) {
        super(registry, false);
        this.collector = collector;
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> holders = super.doScan(basePackages);

        // 扫描结果，注册服务
        holders.forEach(holder -> {
            // 这里扫描到的都是带注解的
            collectService(holder.getBeanDefinition());
        });

        return holders;
    }

    private void collectService(BeanDefinition beanDefinition) {
        // 获取那些带注解的
        String beanClassName = beanDefinition.getBeanClassName();
        try {
            Class<?> aClass = Class.forName(beanClassName);
            Class<?>[] allInterfaces = aClass.getInterfaces();
            if (allInterfaces.length == 0) {
                throw new ClassNotFoundException("No interfaces implements for " + beanClassName);
            }

            RpcService annotation = aClass.getAnnotation(RpcService.class);
            // 通过 annotation 指定的公共接口
            // 并座一层检查
            Class<?> annoInterface = annotation.interfaceClass();

            if (annoInterface == void.class) {
                annoInterface = allInterfaces[0];
            } else {
                // 显式检查注解指定的接口是否被实现
                boolean implementsInterface = false;
                for (Class<?> iface : allInterfaces) {
                    if (annoInterface.isAssignableFrom(iface)) {
                        implementsInterface = true;
                        break;
                    }
                }
                if (!implementsInterface) {
                    throw new IllegalStateException(
                            String.format("Class %s does not implement interface %s specified in @RpcService",
                                    beanClassName,
                                    annoInterface.getName())
                    );
                }
            }
            // 该接口类合法，可以注册服务
            collector.collect(annoInterface);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}