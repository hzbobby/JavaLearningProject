package com.bobby.localmessage;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@MapperScan("com.bobby.localmessage.mapper")
@SpringBootApplication
public class LocalMessageApplication {
    public static void main(String[] args) {
        SpringApplication.run(LocalMessageApplication.class, args);
    }
}
