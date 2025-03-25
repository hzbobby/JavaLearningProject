package com.bobby.bizb.controller;

import com.bobby.bizb.service.IBizBService;
import com.bobby.common.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/biz")
@RestController
public class BizController {


    private final IBizBService bizBService;

    @PostMapping("/do/{id}")
    public Result doBizB(@PathVariable("id") Long id) {
        return bizBService.doBizB(id);
    }
}
