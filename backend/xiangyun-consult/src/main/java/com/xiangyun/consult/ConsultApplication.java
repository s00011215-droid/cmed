package com.xiangyun.consult;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.xiangyun.consult"})
// @EnableDiscoveryClient — disabled in dev mode (no Nacos)
public class ConsultApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsultApplication.class, args);
    }
}
