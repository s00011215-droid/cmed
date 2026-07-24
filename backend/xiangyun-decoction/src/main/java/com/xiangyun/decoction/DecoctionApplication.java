package com.xiangyun.decoction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(scanBasePackages = {"com.xiangyun.decoction", "com.xiangyun.common"})
@MapperScan("com.xiangyun.decoction.mapper")
// @EnableDiscoveryClient — disabled in dev mode (no Nacos)
public class DecoctionApplication {

    public static void main(String[] args) {
        SpringApplication.run(DecoctionApplication.class, args);
    }
}
