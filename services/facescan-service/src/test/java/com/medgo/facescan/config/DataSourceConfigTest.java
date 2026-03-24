package com.medgo.facescan.config;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;

class DataSourceConfigTest {

    @Test
    void membershipDataSource_and_entityManagerFactory_and_txManager() throws Exception {
        MembershipDataSourceConfig cfg = new MembershipDataSourceConfig();
        // set private fields via reflection
        setField(cfg, "jdbcUrl", "jdbc:h2:mem:testdb");
        setField(cfg, "username", "sa");
        setField(cfg, "password", "");
        setField(cfg, "driverClassName", "org.h2.Driver");

        DataSource ds = cfg.membershipDataSource();
        assertNotNull(ds);
        // ensure factory bean can be created with this datasource
        var factoryBean = cfg.membershipEntityManagerFactory(ds);
        assertNotNull(factoryBean);
        assertEquals("membershipPU", factoryBean.getPersistenceUnitName());

        // transaction manager creation should succeed with a mock EntityManagerFactory
        var emf = org.mockito.Mockito.mock(jakarta.persistence.EntityManagerFactory.class);
        var tm = cfg.membershipTransactionManager(emf);
        assertNotNull(tm);
    }

    @Test
    void medigoDataSource_and_entityManagerFactory_and_txManager() throws Exception {
        MedigoDataSourceConfig cfg = new MedigoDataSourceConfig();
        setField(cfg, "jdbcUrl", "jdbc:h2:mem:testdb2");
        setField(cfg, "username", "sa");
        setField(cfg, "password", "");
        setField(cfg, "driverClassName", "org.h2.Driver");

        DataSource ds = cfg.medigoDataSource();
        assertNotNull(ds);
        var factoryBean = cfg.medigoEntityManagerFactory(ds);
        assertNotNull(factoryBean);
        assertEquals("medigoPU", factoryBean.getPersistenceUnitName());

        var emf = org.mockito.Mockito.mock(jakarta.persistence.EntityManagerFactory.class);
        var tm = cfg.medigoTransactionManager(emf);
        assertNotNull(tm);
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
}

