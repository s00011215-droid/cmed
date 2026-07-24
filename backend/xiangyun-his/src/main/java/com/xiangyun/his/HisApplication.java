package com.xiangyun.his;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication(scanBasePackages = {"com.xiangyun.his","com.xiangyun.common"})
@MapperScan("com.xiangyun.his.mapper")
public class HisApplication {
    public static void main(String[] args) { SpringApplication.run(HisApplication.class, args); }
}
