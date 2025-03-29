package com.bobby.bizb;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDubbo
@MapperScan("com.bobby.bizb.mapper")
@SpringBootApplication
public class BizBApplication {
    public static void main(String[] args) {
        SpringApplication.run(BizBApplication.class, args);
    }
}
