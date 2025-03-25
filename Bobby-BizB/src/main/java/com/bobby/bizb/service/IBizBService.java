package com.bobby.bizb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bobby.bizb.domain.BizB;
import com.bobby.common.utils.Result;

public interface IBizBService extends IService<BizB> {
    public Result doBizB(Long id);
    public void sendReplyMsg(Long id);
}
