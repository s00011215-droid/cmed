package com.xiangyun.common.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Common 模組自動配置
 * <p>
 * 掃描 common 包下所有組件，其他微服務只需引入 common 依賴即可自動載入。
 * </p>
 */
@Configuration
@ComponentScan(basePackages = "com.xiangyun.common")
@MapperScan(basePackages = "com.xiangyun.common")
@EnableAspectJAutoProxy
public class CommonAutoConfiguration {
}
