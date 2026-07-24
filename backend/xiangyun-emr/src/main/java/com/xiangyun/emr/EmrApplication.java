package com.xiangyun.emr;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.xiangyun.emr", "com.xiangyun.common"})
@MapperScan("com.xiangyun.emr.mapper")
public class EmrApplication {
    public static void main(String[] args) { SpringApplication.run(EmrApplication.class, args); }
}
