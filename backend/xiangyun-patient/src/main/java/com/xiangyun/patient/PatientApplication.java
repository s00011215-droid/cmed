package com.xiangyun.patient;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.xiangyun.patient", "com.xiangyun.common"})
@MapperScan("com.xiangyun.patient.mapper")
public class PatientApplication {
    public static void main(String[] args) { SpringApplication.run(PatientApplication.class, args); }
}
