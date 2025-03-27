package com.bobby.localmessage;

import com.bobby.localmessage.domain.LocalMessageDO;
import com.bobby.localmessage.service.ILocalBizService;
import com.bobby.localmessage.service.ILocalMessageDOService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("local")
@SpringBootTest
public class TestDemo {

    @Resource
    private ILocalBizService localBizService;

    @Resource
    private ILocalMessageDOService localMessageDOService;

    @Test
    public void testLocalMsg() {
        localBizService.doBiz();
    }

    @Test
    public void writeLocalMsgTest(){
        LocalMessageDO demo = LocalMessageDO.of("demo", 5, 2);
        localMessageDOService.save(demo);
    }

}
