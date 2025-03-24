package com.bobby.aspect.shows;

import org.springframework.stereotype.Component;

@Component
public class SleepNoMoreDrama implements IPerformance {
    @Override
    public void perform() {
//        int i = 3/0; // 异常
        System.out.println("正在表演 Sleep NoMore");
    }
}
