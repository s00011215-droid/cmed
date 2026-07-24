package com.xiangyun.prescription;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.xiangyun.prescription"})
// @EnableDiscoveryClient — disabled in dev mode (no Nacos)
public class PrescriptionApplication {
    public static void main(String[] args) { SpringApplication.run(PrescriptionApplication.class, args); }
}
