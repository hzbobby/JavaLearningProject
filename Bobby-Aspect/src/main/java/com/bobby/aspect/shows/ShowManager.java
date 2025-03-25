package com.bobby.aspect.shows;

import lombok.Data;

@Data
public class ShowManager implements IPerformance {
    private IPerformance whoPerformance;

    @Override
    public void perform() {
        System.out.println("话事人: 布置舞台...");
        whoPerformance.perform();
        System.out.println("话事人: 收拾舞台...");
    }
}
