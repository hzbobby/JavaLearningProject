package com.bobby.biza;

import com.bobby.common.service.IDubboDemoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@ActiveProfiles("local")
@SpringBootTest
public class DubboDemoTest {
    // 使用 DubboReference 引入服务
    // 这其实就是一个发现服务的过程
    @DubboReference
    IDubboDemoService demoService;


    @Test
    public void remoteCall() {
        log.info("remoteCall: "+ demoService.getDemo());
    }
}
