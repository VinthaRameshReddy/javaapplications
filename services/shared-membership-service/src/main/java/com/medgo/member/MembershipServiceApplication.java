package com.medgo.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;


@SpringBootApplication
@ComponentScan(
        basePackages = {
                "com.medgo.member",
                "com.medgo.crypto"
        })
//@EnableFeignClients
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class  MembershipServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MembershipServiceApplication.class, args);
    }
}
