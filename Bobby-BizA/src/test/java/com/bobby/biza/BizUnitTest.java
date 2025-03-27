package com.bobby.biza;

import com.bobby.biza.service.IBizAService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("local")
@SpringBootTest
public class BizUnitTest {

    @Resource
    private IBizAService bizAService;

    @Test
    public void doBiz() {
        Boolean b = bizAService.doBizA().getSuccess();
    }
}
