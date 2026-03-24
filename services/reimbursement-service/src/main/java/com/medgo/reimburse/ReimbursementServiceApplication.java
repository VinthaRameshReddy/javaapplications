package com.medgo.reimburse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
@ComponentScan(basePackages = {
        "com.medgo.reimburse", "com.medgo.config", "com.medgo.filter", "com.medgo.jwt", "com.medgo.crypto", "com.medgo.enums"
        // scans all components, controllers, services under com.medgo.*
})
@EntityScan(basePackages = {
        "com.medgo.reimburse.entity",
        "com.medgo.utils"})
public class ReimbursementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReimbursementServiceApplication.class, args);
    }
}
