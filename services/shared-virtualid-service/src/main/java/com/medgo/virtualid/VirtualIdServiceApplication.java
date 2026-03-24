package com.medgo.virtualid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(
        exclude = {
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration.class
        }
)
@ComponentScan(basePackages = {
        "com.medgo.virtualid", "com.medgo.crypto",
})

@EnableAspectJAutoProxy(proxyTargetClass = true)
public class VirtualIdServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(VirtualIdServiceApplication.class, args);
    }
}
