package com.bobby.bizb;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.bobby.bizb.mapper")
@SpringBootApplication
public class BizBApplication {
    public static void main(String[] args) {
        SpringApplication.run(BizBApplication.class, args);
    }
}
