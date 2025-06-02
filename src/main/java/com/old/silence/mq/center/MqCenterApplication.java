package com.old.silence.mq.center;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author MurrayZhang
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class MqCenterApplication {
    public static void main(String[] args) {
        SpringApplication.run(MqCenterApplication.class, args);
    }
}
