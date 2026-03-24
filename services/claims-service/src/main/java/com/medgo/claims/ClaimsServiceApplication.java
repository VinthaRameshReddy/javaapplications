package com.medgo.claims;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("com.medgo.claims.domain.entity")
@ComponentScan(
        basePackages = {
                "com.medgo.claims",
                "com.medgo.crypto",
                "com.medgo.enums",
                "com.medgo.config",
                "com.medgo.filter",
                "com.medgo.jwt"
        }
)
@EnableFeignClients(basePackages = "com.medgo.claims.feign")
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class ClaimsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClaimsServiceApplication.class, args);
    }
}
