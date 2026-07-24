package com.xiangyun.logistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(scanBasePackages = {"com.xiangyun.logistics", "com.xiangyun.common"})
@MapperScan("com.xiangyun.logistics.mapper")
// @EnableDiscoveryClient — disabled in dev mode (no Nacos)
public class LogisticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogisticsApplication.class, args);
    }
}
