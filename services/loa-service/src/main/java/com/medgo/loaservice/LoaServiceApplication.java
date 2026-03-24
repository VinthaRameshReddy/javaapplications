package com.medgo.loaservice;

import com.medgo.config.JwtWebSecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication(
        exclude = {
                DataSourceAutoConfiguration.class,
                DataSourceTransactionManagerAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class
        }
)
@ComponentScan(
        basePackages = {
                "com.medgo.loaservice",
                "com.medgo.crypto",
                "com.medgo.config",
                "com.medgo.filter",
                "com.medgo.jwt"
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtWebSecurityConfig.class
        )
)
@EnableFeignClients(basePackages = "com.medgo.loaservice.feign")
public final class LoaServiceApplication {

    private LoaServiceApplication() {
        // Utility class - prevent instantiation
    }

    public static void main(String[] args) {
        SpringApplication.run(LoaServiceApplication.class, args);
    }
}
