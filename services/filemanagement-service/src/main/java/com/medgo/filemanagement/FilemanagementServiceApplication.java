package com.medgo.filemanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(
        basePackages = {
                "com.medgo.filemanagement",
                "com.medgo.member"
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = com.medgo.config.JwtWebSecurityConfig.class
        )
)
//@EnableFeignClients
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class  FilemanagementServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FilemanagementServiceApplication.class, args);
    }
}
