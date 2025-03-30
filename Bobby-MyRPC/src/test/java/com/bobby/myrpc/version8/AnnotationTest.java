package com.bobby.myrpc.version8;

import com.bobby.myrpc.version8.biz.service.IRpcDemoService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/3/30
 */
@SpringBootTest
@ActiveProfiles("home")
public class AnnotationTest {
    @Resource
    IRpcDemoService demoService;

    @Test
    public void testRpcReference() {
        demoService.doSomething();
    }
}
