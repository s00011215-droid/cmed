package com.xiangyun.his;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
@SpringBootApplication(scanBasePackages = {"com.xiangyun.his","com.xiangyun.common"})
@EnableDiscoveryClient
public class HisApplication {
    public static void main(String[] args) { SpringApplication.run(HisApplication.class, args); }
}
