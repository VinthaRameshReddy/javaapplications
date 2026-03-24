package com.medgo.member;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class UtilizationServiceApplicationTest {


    @Test
    void testMainMethodWithNoArguments() {
        // Mock SpringApplication.run()
        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {

            mocked.when(() -> SpringApplication.run(
                    any(Class.class), any(String[].class)
            )).thenReturn(null);

            // Call main method
            UtilizationServiceApplication.main(new String[]{});

            // Verify run() was called exactly once
            mocked.verify(() ->
                    SpringApplication.run(UtilizationServiceApplication.class, new String[]{}), Mockito.times(1)
            );
        }
    }

    @Test
    void testMainMethodWithArguments() {
        // Test main method with actual arguments
        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {
            String[] args = {"--server.port=8080", "--spring.profiles.active=test"};

            mocked.when(() -> SpringApplication.run(
                    any(Class.class), any(String[].class)
            )).thenReturn(null);

            // Call main method with arguments
            UtilizationServiceApplication.main(args);

            // Verify run() was called with the exact arguments
            mocked.verify(() ->
                    SpringApplication.run(UtilizationServiceApplication.class, args), Mockito.times(1)
            );
        }
    }

    @Test
    void testConstructorInstantiation() {
        // Test that the class can be instantiated
        UtilizationServiceApplication application = new UtilizationServiceApplication();
        assertNotNull(application, "Application instance should not be null");
    }

    @Test
    void testSpringBootApplicationAnnotation() {
        // Verify @SpringBootApplication annotation is present
        SpringBootApplication annotation = UtilizationServiceApplication.class
                .getAnnotation(SpringBootApplication.class);
        assertNotNull(annotation, "@SpringBootApplication annotation should be present");
    }

    @Test
    void testComponentScanAnnotation() {
        // Verify @ComponentScan annotation is present and configured correctly
        ComponentScan componentScan = UtilizationServiceApplication.class
                .getAnnotation(ComponentScan.class);

        assertNotNull(componentScan, "@ComponentScan annotation should be present");

        // Verify base packages
        String[] basePackages = componentScan.basePackages();
        assertNotNull(basePackages, "Base packages should not be null");
        assertEquals(3, basePackages.length, "Should have 3 base packages");
        assertArrayEquals(
                new String[]{"com.medgo.member", "com.medgo.crypto", "com.medgo.config"},
                basePackages,
                "Base packages should match expected values"
        );

        // Verify exclude filters
        ComponentScan.Filter[] excludeFilters = componentScan.excludeFilters();
        assertNotNull(excludeFilters, "Exclude filters should not be null");
        assertEquals(1, excludeFilters.length, "Should have 1 exclude filter");
        assertEquals(FilterType.ASSIGNABLE_TYPE, excludeFilters[0].type(), "Filter type should be ASSIGNABLE_TYPE");
        assertArrayEquals(
                new Class[]{com.medgo.config.JwtWebSecurityConfig.class},
                excludeFilters[0].classes(),
                "Excluded class should be JwtWebSecurityConfig"
        );
    }

    @Test
    void testEnableAspectJAutoProxyAnnotation() {
        // Verify @EnableAspectJAutoProxy annotation is present
        EnableAspectJAutoProxy aspectJAutoProxy = UtilizationServiceApplication.class
                .getAnnotation(EnableAspectJAutoProxy.class);

        assertNotNull(aspectJAutoProxy, "@EnableAspectJAutoProxy annotation should be present");
        assertTrue(aspectJAutoProxy.proxyTargetClass(), "proxyTargetClass should be true");
    }

    @Test
    void testClassIsPublic() {
        // Verify the class is public
        assertTrue(
                java.lang.reflect.Modifier.isPublic(UtilizationServiceApplication.class.getModifiers()),
                "UtilizationServiceApplication class should be public"
        );
    }

    @Test
    void testMainMethodIsPublicStatic() throws NoSuchMethodException {
        // Verify main method exists and is public static
        java.lang.reflect.Method mainMethod = UtilizationServiceApplication.class
                .getMethod("main", String[].class);

        assertNotNull(mainMethod, "main method should exist");
        assertTrue(
                java.lang.reflect.Modifier.isPublic(mainMethod.getModifiers()),
                "main method should be public"
        );
        assertTrue(
                java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers()),
                "main method should be static"
        );
        assertEquals(void.class, mainMethod.getReturnType(), "main method should return void");
    }

    @Test
    void testMultipleInstantiations() {
        // Test creating multiple instances
        UtilizationServiceApplication app1 = new UtilizationServiceApplication();
        UtilizationServiceApplication app2 = new UtilizationServiceApplication();

        assertNotNull(app1);
        assertNotNull(app2);
        assertNotSame(app1, app2, "Each instance should be unique");
    }

    @Test
    void testMainMethodWithNullArguments() {
        // Test main method with null arguments (edge case)
        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {
            mocked.when(() -> SpringApplication.run(
                    any(Class.class), any(String[].class)
            )).thenReturn(null);

            // Call main method with null
            UtilizationServiceApplication.main(null);

            // Verify run() was called
            mocked.verify(() ->
                    SpringApplication.run(UtilizationServiceApplication.class, (String[]) null), Mockito.times(1)
            );
        }
    }
}
