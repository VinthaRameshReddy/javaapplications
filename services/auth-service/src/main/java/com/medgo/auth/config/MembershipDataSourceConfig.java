package com.medgo.auth.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.medgo.auth.repository.membership",  // Repositories for membership DB
        entityManagerFactoryRef = "membershipEntityManagerFactory",
        transactionManagerRef = "membershipTransactionManager"
)
public class MembershipDataSourceConfig {

    @Bean(name = "membershipDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.membership")
    public DataSource membershipDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "membershipEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean membershipEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("membershipDataSource") DataSource dataSource) {

        // Hibernate-specific properties for schema creation
        HashMap<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.hbm2ddl.auto", "update");
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect");

        return builder
                .dataSource(dataSource)
                .packages("com.medgo.auth.domain.entity.membership")
                .persistenceUnit("membershipPU")
                .properties(jpaProperties)
                .build();
    }

    @Bean(name = "membershipTransactionManager")
    public PlatformTransactionManager membershipTransactionManager(
            @Qualifier("membershipEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
