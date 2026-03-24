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
        basePackages = "com.medgo.member.repository.membership",
        entityManagerFactoryRef = "membershipEntityManagerFactory",
        transactionManagerRef = "membershipTransactionManager"
)
public class MembershipDataSourceConfig {

    @Value("${spring.datasource.membership.jdbc-url}")
    private String jdbcUrl;

    @Value("${spring.datasource.membership.username}")
    private String username;

    @Value("${spring.datasource.membership.password}")
    private String password;

    @Value("${spring.datasource.membership.driver-class-name}")
    private String driverClassName;

    @Bean(name = "membershipDataSource")
    public DataSource membershipDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        return dataSource;
    }

    @Bean(name = "membershipEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean membershipEntityManagerFactory(
            @Qualifier("membershipDataSource") DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.medgo.member.domain.entity.membership");
        em.setPersistenceUnitName("membershipPU");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Properties props = new Properties();
        props.setProperty("hibernate.hbm2ddl.auto", "none");
        props.setProperty("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect");
        em.setJpaProperties(props);

        return em;
    }

    @Bean(name = "membershipTransactionManager")
    public PlatformTransactionManager membershipTransactionManager(
            @Qualifier("membershipEntityManagerFactory") jakarta.persistence.EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
