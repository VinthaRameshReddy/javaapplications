package com.medgo.reimburse.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.medgo.reimburse.repository.reimb",
        entityManagerFactoryRef = "reimbEntityManagerFactory",
        transactionManagerRef = "reimbTransactionManager"
)
public class ReimbDataSourceConfig {

    @Primary
    @Bean(name = "reimbDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.reim")
    public DataSource reimbDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "reimbEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean reimbEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("reimbDataSource") DataSource dataSource) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect");

        return builder
                .dataSource(dataSource)
                .packages("com.medgo.reimburse.domain.entity.reimb")
                .persistenceUnit("reimbPU")
                .properties(properties)
                .build();
    }

    @Primary
    @Bean(name = "reimbTransactionManager")
    public PlatformTransactionManager reimbTransactionManager(
            @Qualifier("reimbEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
