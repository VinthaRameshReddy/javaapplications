package com.medgo.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
@SpringBootApplication
@ComponentScan(basePackages = {
        "com.medgo.auth", "com.medgo.config", "com.medgo.filter", "com.medgo.jwt", "com.medgo.crypto",
        "com.medgo.auditingutils",
        "com.medgo.utils.service"
})
@EntityScan(basePackages = {
        "com.medgo.auth.domain.entity.medigo",
        "com.medgo.auth.domain.entity.membership",
        "com.medgo.utils.domain"
})
@EnableFeignClients(basePackages = "com.medgo.auth.clients")
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}