package com.xiangyun.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(scanBasePackages = {"com.xiangyun.account", "com.xiangyun.common"})
@MapperScan("com.xiangyun.account.mapper")
// @EnableDiscoveryClient — disabled in dev mode (no Nacos)
public class AccountApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountApplication.class, args);
    }
}
