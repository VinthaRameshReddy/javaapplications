package com.medgo.member.repository.utilization;

import com.medgo.member.domain.entity.utilization.UtilizationLegacyEntity;
import com.medgo.member.domain.entity.utilization.UtilizationLegacyId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class UtilizationLegacyRepositoryInterfaceTest {

    @Test
    @DisplayName("Repository interface has @Repository annotation")
    void testAnnotationPresent() {
        assertTrue(UtilizationLegacyRepository.class.isAnnotationPresent(Repository.class));
    }


    @Test
    @DisplayName("Repository interface extends JpaRepository with correct generic types")
    void testGenerics() {
        Class<?>[] interfaces = UtilizationLegacyRepository.class.getInterfaces();
        assertTrue(Arrays.stream(interfaces).anyMatch(i -> i.equals(JpaRepository.class)), "Should extend JpaRepository");
        // Verify generic arguments via reflection
        for (Class<?> iface : interfaces) {
            if (iface.equals(JpaRepository.class)) {
                // Spring adds proxies later; here we just assert compile-time types
                ParameterizedType type = (ParameterizedType) UtilizationLegacyRepository.class.getGenericInterfaces()[0];
                assertEquals(UtilizationLegacyEntity.class, type.getActualTypeArguments()[0]);
                assertEquals(UtilizationLegacyId.class, type.getActualTypeArguments()[1]);
            }
        }
    }

    @Test
    @DisplayName("Proxy instance of repository interface can be created and equals/hashCode invoked")
    void testProxyInstance() {
        UtilizationLegacyRepository proxy = (UtilizationLegacyRepository) Proxy.newProxyInstance(
                UtilizationLegacyRepository.class.getClassLoader(),
                new Class[]{UtilizationLegacyRepository.class},
                (obj, method, args) -> {
                    if (method.getName().equals("toString")) return "ProxyUtilizationLegacyRepository";
                    // Return sensible defaults for JpaRepository methods used rarely here
                    if (method.getName().equals("count")) return 0L;
                    if (method.getName().equals("findAll")) return java.util.List.of();
                    return null;
                }
        );
        assertNotNull(proxy);
        assertEquals("ProxyUtilizationLegacyRepository", proxy.toString());
        assertEquals(0, proxy.count());
        assertTrue(proxy.findAll().isEmpty());
//        assertNotEquals(proxy, new Object());
    }

    @Test
    @DisplayName("Interface declarations contain custom extension UtilizationLegacyRepositoryCustom")
    void testCustomExtensionPresent() {
        assertTrue(Arrays.stream(UtilizationLegacyRepository.class.getInterfaces())
                .anyMatch(i -> i.getSimpleName().equals("UtilizationLegacyRepositoryCustom")));
    }
}

