package com.bobby.biza.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bobby.biza.domain.LocalMessage;
import com.bobby.biza.mapper.LocalMessageMapper;
import com.bobby.biza.service.ILocalMessageService;
import org.springframework.stereotype.Service;

@Service
public class LocalMessageServiceImpl extends ServiceImpl<LocalMessageMapper, LocalMessage> implements ILocalMessageService {

}
