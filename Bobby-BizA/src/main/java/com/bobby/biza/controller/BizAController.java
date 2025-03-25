package com.bobby.biza.controller;

import com.bobby.biza.service.IBizAService;
import com.bobby.common.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/biz/")
@RestController
public class BizAController {

    private final IBizAService bizAService;

    @PostMapping("/do")
    public Result doBizA() {
        return bizAService.doBizA();
    }
}
