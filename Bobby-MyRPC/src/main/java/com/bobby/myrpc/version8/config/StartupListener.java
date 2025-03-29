package com.bobby.myrpc.version8.config;

import com.bobby.myrpc.version8.register.IServiceRegister;
import com.bobby.myrpc.version8.register.ServiceCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * 监听容器刷新完成事件（所有Bean已初始化完毕）
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {
    private final ServiceCollector serviceCollector;
    private final IServiceRegister serviceRegister;


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 防止子容器触发重复执行（父子容器场景）
        if (event.getApplicationContext().getParent() == null) {
            doAfterStartup();
        }
    }

    private void doAfterStartup() {
        // 注册服务
        log.info("开始注册服务");
    }
}