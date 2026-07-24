package com.xiangyun.finance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(scanBasePackages = {"com.xiangyun.finance", "com.xiangyun.common"})
@MapperScan("com.xiangyun.finance.mapper")
// @EnableDiscoveryClient — disabled in dev mode (no Nacos)
public class FinanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceApplication.class, args);
    }
}
