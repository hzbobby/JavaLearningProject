package com.bobby.myrpc.version8;

import com.bobby.myrpc.version8.config.MyRPCAutoConfiguration;
import com.bobby.myrpc.version8.config.MyRPCProperties;
import com.bobby.myrpc.version8.config.ZkProperties;
import com.bobby.myrpc.version8.register.ZkServiceRegister;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("local")
@Slf4j
@SpringBootTest(classes = {MyRPCAutoConfiguration.class})
public class SpringConfigTest {
    @Resource
    MyRPCProperties myRPCProperties;
    @Resource
    ZkProperties zkProperties;

    @Resource
    private ZkServiceRegister zkServiceRegister;

    @Test
    void autoConfig() {
        assert zkServiceRegister != null;
        log.info("MyRPCProperties: {}", myRPCProperties);
        log.info("ZkProperties: {}", zkProperties);
    }
}
