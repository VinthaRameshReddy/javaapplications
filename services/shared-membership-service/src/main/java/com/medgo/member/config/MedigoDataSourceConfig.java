package com.medgo.member.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.medgo.member.repository.medgo",
        entityManagerFactoryRef = "medigoEntityManagerFactory",
        transactionManagerRef = "medigoTransactionManager"
)
public class MedigoDataSourceConfig {

    @Value("${spring.datasource.medigo.jdbc-url}")
    private String jdbcUrl;

    @Value("${spring.datasource.medigo.username}")
    private String username;

    @Value("${spring.datasource.medigo.password}")
    private String password;

    @Value("${spring.datasource.medigo.driver-class-name}")
    private String driverClassName;

    @Bean(name = "medigoDataSource")
    public DataSource medigoDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        return dataSource;
    }

    @Bean(name = "medigoEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean medigoEntityManagerFactory(
            @Qualifier("medigoDataSource") DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.medgo.member.domain.entity.medgo");
        em.setPersistenceUnitName("medigoPU");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Properties props = new Properties();
        props.setProperty("hibernate.hbm2ddl.auto", "none");
        props.setProperty("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect");
        em.setJpaProperties(props);

        return em;
    }

    @Bean(name = "medigoTransactionManager")
    public PlatformTransactionManager medigoTransactionManager(
            @Qualifier("medigoEntityManagerFactory") jakarta.persistence.EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}