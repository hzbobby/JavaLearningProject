package com.bobby.localmessage.service.impl;

import com.bobby.localmessage.service.ILocalBizService;
import com.bobby.localmessage.service.IThirdPartyInvokeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class LocalBizServiceImpl implements ILocalBizService {

    private final IThirdPartyInvokeService thirdPartyInvokeService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void doBiz() {
        log.info("now, we do local Biz");
        // 三方调用
        thirdPartyInvokeService.thirdPartyInvoke();
        log.info("local biz done");
    }
}
