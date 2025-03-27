package com.bobby.biza.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bobby.biza.domain.LocalMessageDO;

public interface ILocalMessageDOService extends IService<LocalMessageDO> {
    public void invoke(LocalMessageDO localMessageDO, boolean async);
    public void increaseInvokeTimes(Long id);
}
