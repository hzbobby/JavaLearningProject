package com.bobby.myrpc.version8.prosessor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/3/30
 */
@Slf4j
public class RpcServiceRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware {
    private static ApplicationContext context;
    private ServerProperties serverProperties;

    public RpcServiceRegistryPostProcessor() {

    }

    public RpcServiceRegistryPostProcessor(ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
//        // 1. 扫描所有带有 @RpcService 的类（不实例化 Bean）
//        ClassPathScanningCandidateComponentProvider scanner =
//                new ClassPathScanningCandidateComponentProvider(false);
//        scanner.addIncludeFilter(new AnnotationTypeFilter(RpcService.class));
//
//        Set<BeanDefinition> candidates = scanner.findCandidateComponents("com.bobby.myrpc.version8.biz.service"); // 替换为你的包路径
//        this.serverProperties = context.getBean(ServerProperties.class);
//        IServiceRegister serviceRegister = context.getBean(IServiceRegister.class);
//        // 2. 提前注册服务到注册中心
//        for (BeanDefinition candidate : candidates) {
//            try {
//                Class<?> clazz = Class.forName(candidate.getBeanClassName());
//                RpcService rpcService = clazz.getAnnotation(RpcService.class);
//                registerService(clazz, rpcService, serviceRegister);
//            } catch (Exception e) {
//                log.error("预注册RPC服务失败: {}", candidate.getBeanClassName(), e);
//            }
//        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // 可留空或补充其他逻辑
    }

//    private void registerService(Class<?> serviceClass, RpcService rpcService, IServiceRegister serviceRegister) {
//        Class<?> interfaceClass = rpcService.interfaceClass();
//        if (interfaceClass == void.class) {
//            interfaceClass = serviceClass.getInterfaces()[0]; // 默认取第一个接口
//        }
//        String serviceName = interfaceClass.getName();
//        InetSocketAddress address = new InetSocketAddress(
//                serverProperties.getAddress(),
//                serverProperties.getPort()
//        );
//        serviceRegister.register(serviceName, address);
//        log.info("预注册RPC服务成功: {} -> {}", serviceName, address);
//    }
}
