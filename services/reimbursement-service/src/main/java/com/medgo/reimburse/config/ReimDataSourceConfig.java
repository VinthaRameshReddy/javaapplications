package com.medgo.reimburse.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.medgo.reimburse.repository",
        entityManagerFactoryRef = "reimEntityManagerFactory",
        transactionManagerRef = "reimTransactionManager"
)
public class ReimDataSourceConfig {

    @Primary
    @Bean(name = "reimDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.reim")
    public DataSource reimDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "reimEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean reimEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("reimDataSource") DataSource dataSource) {

        HashMap<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.hbm2ddl.auto", "update"); // 👈 forces schema creation/update
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect");

        return builder
                .dataSource(dataSource)
                .packages("com.medgo.reimburse.domain.entity") // entities for reim db
                .persistenceUnit("reimPU")
                .properties(jpaProperties)
                .build();
    }

    @Primary
    @Bean(name = "reimTransactionManager")
    public PlatformTransactionManager reimTransactionManager(
            @Qualifier("reimEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
