package com.bobby.rpc.consumer;

import com.bobby.rpc.consumer.service.IConsumerService;
import jakarta.annotation.Resource;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("lab")
@SpringBootTest
public class RpcReferenceTest {
    @Resource
    IConsumerService consumerService;

    @Test
    public void test() {
        consumerService.consume();
    }
}
