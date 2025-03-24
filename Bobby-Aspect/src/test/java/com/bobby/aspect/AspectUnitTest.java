package com.bobby.aspect;

import com.bobby.aspect.shows.JayChouPerformance;
import com.bobby.aspect.shows.SleepNoMoreDrama;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AspectUnitTest {

    @Resource
    JayChouPerformance jayChouPerformance;
    @Resource
    SleepNoMoreDrama sleepNoMoreDrama;


    @Test
    public void performShows() {
        ShowManager showManager = new ShowManager();
        showManager.setWhoPerformance(jayChouPerformance);
        showManager.perform();
    }
}
