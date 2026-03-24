package com.medgo.reimburse.config;

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
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.medgo.reimburse.repository.medigo",
        entityManagerFactoryRef = "medigoEntityManagerFactory",
        transactionManagerRef = "medigoTransactionManager"
)
public class MedigoDataSourceConfig {

    @Bean(name = "medigoDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.medigo")
    public DataSource medigoDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "medigoEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean medigoEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("medigoDataSource") DataSource dataSource) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect");

        return builder
                .dataSource(dataSource)
                .packages("com.medgo.reimburse.domain.entity.medigo")
                .persistenceUnit("medigoPU")
                .properties(properties)
                .build();
    }

    @Bean(name = "medigoTransactionManager")
    public PlatformTransactionManager medigoTransactionManager(
            @Qualifier("medigoEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
