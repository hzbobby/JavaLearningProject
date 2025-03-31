package com.bobby.myrpc.version8;

import com.bobby.myrpc.version8.biz.service.IUserService;
import com.bobby.myrpc.version8.client.RpcClientProxy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/3/30
 */
@SpringBootTest
@ActiveProfiles("home")
@Slf4j
public class ProxyTest {
    @Resource
    RpcClientProxy rpcClientProxy;

    @Test
    void testProxy(){
        log.info("ProxyTest");
        IUserService proxy = rpcClientProxy.getProxy(IUserService.class);
        assert proxy != null;
    }
}
