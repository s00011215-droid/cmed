package com.xiangyun.prescription.config;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
@Configuration
@AutoConfigureBefore(MybatisPlusAutoConfiguration.class)
public class MybatisPlusConfig {
    @PostConstruct
    void init() {
        JacksonTypeHandler.setObjectMapper(
            new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        );
    }
}
