package com.bobby.localmessage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bobby.localmessage.domain.LocalMessageDO;

import java.util.List;

public interface ILocalMessageDOService extends IService<LocalMessageDO> {
    public void invoke(LocalMessageDO localMessageDO, boolean async);

    public List<LocalMessageDO> loadWaitRetryRecords();
}
