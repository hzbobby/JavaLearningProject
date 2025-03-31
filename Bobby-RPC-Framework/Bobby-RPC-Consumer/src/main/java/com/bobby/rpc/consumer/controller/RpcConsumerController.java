package com.bobby.rpc.consumer.controller;

import com.bobby.rpc.consumer.service.IConsumerService;
import com.bobby.rpc.core.sample.IDemoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rpc/consumer/")
public class RpcConsumerController {
    @Resource
    IConsumerService consumerService;

    @PostMapping("/doBiz")
    public void doBiz() {
        consumerService.consume();
    }
}
