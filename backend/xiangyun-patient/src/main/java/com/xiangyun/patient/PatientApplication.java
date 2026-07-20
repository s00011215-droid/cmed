package com.xiangyun.patient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
@SpringBootApplication(scanBasePackages = {"com.xiangyun.patient","com.xiangyun.common"})
@EnableDiscoveryClient
public class PatientApplication {
    public static void main(String[] args) { SpringApplication.run(PatientApplication.class, args); }
}
