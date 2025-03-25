package com.bobby.aspect.config;

import com.bobby.aspect.aspect.ShowAspect;
import com.bobby.aspect.shows.JayChouPerformance;
import com.bobby.aspect.shows.SleepNoMoreDrama;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy
@Configuration
public class AspectConfig {
    @Bean
    public ShowAspect showAspect() {
        return new ShowAspect();
    }

    @Bean
    public JayChouPerformance jayChouPerformance() {
        return new JayChouPerformance();
    }

    @Bean
    public SleepNoMoreDrama sleepNoMoreDrama() {
        return new SleepNoMoreDrama();
    }

}
