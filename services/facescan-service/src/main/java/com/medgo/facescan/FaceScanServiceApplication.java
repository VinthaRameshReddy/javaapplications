package com.medgo.facescan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@ComponentScan(basePackages = {
        "com.medgo.facescan",
        "com.medgo.config",
        "com.medgo.filter",
        "com.medgo.jwt",
        "com.medgo.crypto",
        "com.medgo.encryption",
        "com.medgo.commons"
})
@EntityScan(basePackages = {
        "com.medgo.facescan.domain.models.medgo",
        "com.medgo.facescan.domain.models.membership",
        "com.medgo.utils"
})
public class FaceScanServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FaceScanServiceApplication.class, args);
    }
}