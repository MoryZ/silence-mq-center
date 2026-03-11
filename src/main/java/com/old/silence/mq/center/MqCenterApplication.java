package com.old.silence.mq.center;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author MurrayZhang
 */
@SpringBootApplication
@MapperScan("com.old.silence.mq.center.domain.repository")
public class MqCenterApplication {
    public static void main(String[] args) {
        SpringApplication.run(MqCenterApplication.class, args);
    }
}
