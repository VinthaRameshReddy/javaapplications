package com.medgo.provider;

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
                "com.medgo.provider",
                "com.medgo.crypto",
                "com.medgo.config",
                "com.medgo.filter",
                "com.medgo.jwt"
        }
)
@EnableFeignClients(basePackages = "com.medgo.provider.feign")
public class ProviderGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProviderGatewayApplication.class, args);
    }
}


