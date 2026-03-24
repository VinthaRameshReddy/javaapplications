package com.medgo.reimburse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


@SpringBootApplication
@EnableFeignClients(basePackages = "com.medgo.reimburse.feign")
@ComponentScan(basePackages = {
        "com.medgo.reimburse",
        "com.medgo.crypto",  // <-- ensure crypto package is scanned
        "com.medgo.enums"
})
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class SharedReimbursementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SharedReimbursementServiceApplication.class, args);
    }
}
