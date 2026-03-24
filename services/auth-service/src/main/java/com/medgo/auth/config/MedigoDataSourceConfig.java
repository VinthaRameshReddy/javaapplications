package com.medgo.auth.config;

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

@Configuration
@EnableJpaRepositories(
        basePackages = {"com.medgo.auth.repository.medigo", "com.medgo.utils.repository"},
        entityManagerFactoryRef = "medigoEntityManagerFactory",
        transactionManagerRef = "medigoTransactionManager"
)
public class MedigoDataSourceConfig {

    @Primary
    @Bean(name = "medigoDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.medigo")
    public DataSource medigoDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "medigoEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean medigoEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("medigoDataSource") DataSource dataSource) {

        HashMap<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.hbm2ddl.auto", "update"); // 👈 forces schema creation/update
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect");
        jpaProperties.put("hibernate.hbm2ddl.jdbc_metadata_extraction_strategy", "individually");
        jpaProperties.put("hibernate.format_sql", "true");
        jpaProperties.put("hibernate.use_sql_comments", "true");

        return builder
                .dataSource(dataSource)
                .packages("com.medgo.auth.domain.entity.medigo", "com.medgo.utils.domain") // entities for medigo db including audit entities
                .persistenceUnit("medigoPU")
                .properties(jpaProperties)
                .build();
    }

    @Primary
    @Bean(name = "medigoTransactionManager")
    public PlatformTransactionManager medigoTransactionManager(
            @Qualifier("medigoEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
