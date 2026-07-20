package com.xiangyun.prescription;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
@SpringBootApplication(scanBasePackages = {"com.xiangyun.prescription","com.xiangyun.common"})
@EnableDiscoveryClient
public class PrescriptionApplication {
    public static void main(String[] args) { SpringApplication.run(PrescriptionApplication.class, args); }
}
