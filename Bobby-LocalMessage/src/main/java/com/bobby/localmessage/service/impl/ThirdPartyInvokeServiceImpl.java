package com.bobby.localmessage.service.impl;

import com.bobby.localmessage.annotation.LocalMessage;
import com.bobby.localmessage.service.IThirdPartyInvokeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ThirdPartyInvokeServiceImpl implements IThirdPartyInvokeService {

    @LocalMessage(maxRetryTimes = 5, async = true)
    @Override
    public void thirdPartyInvoke() {
        log.info("I'm third party invoke");
    }
}
