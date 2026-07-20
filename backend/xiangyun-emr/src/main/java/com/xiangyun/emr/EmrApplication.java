package com.xiangyun.emr;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
@SpringBootApplication(scanBasePackages = {"com.xiangyun.emr","com.xiangyun.common"})
@EnableDiscoveryClient
public class EmrApplication {
    public static void main(String[] args) { SpringApplication.run(EmrApplication.class, args); }
}
