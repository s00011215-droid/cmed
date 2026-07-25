package com.xiangyun.prescription;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.xiangyun.prescription.mapper")
@SpringBootApplication(scanBasePackages = {"com.xiangyun.prescription", "com.xiangyun.common"})
// @EnableDiscoveryClient — disabled in dev mode (no Nacos)
public class PrescriptionApplication {
    public static void main(String[] args) { SpringApplication.run(PrescriptionApplication.class, args); }
}
