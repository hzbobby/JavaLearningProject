package com.bobby.biza.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bobby.biza.domain.BizA;
import com.bobby.common.utils.Result;

public interface IBizAService extends IService<BizA> {
    public Result doBizA();
    public void sendMsg(Long id);
}
