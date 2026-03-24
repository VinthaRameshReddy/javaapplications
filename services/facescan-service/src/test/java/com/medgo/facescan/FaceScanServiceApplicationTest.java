package com.medgo.facescan;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class FaceScanServiceApplicationTest {

    @Test
    void classHasSpringBootApplicationAnnotation_withExpectedExclude() {
        Class<FaceScanServiceApplication> cls = FaceScanServiceApplication.class;
        assertTrue(cls.isAnnotationPresent(SpringBootApplication.class), "@SpringBootApplication should be present");

        SpringBootApplication ann = cls.getAnnotation(SpringBootApplication.class);
        Class<?>[] excludes = ann.exclude();
        assertNotNull(excludes);
        assertTrue(Arrays.asList(excludes).contains(DataSourceAutoConfiguration.class), "DataSourceAutoConfiguration must be excluded");
    }

    @Test
    void classHasComponentScan_withExpectedPackages() {
        Class<FaceScanServiceApplication> cls = FaceScanServiceApplication.class;
        assertTrue(cls.isAnnotationPresent(ComponentScan.class), "@ComponentScan should be present");

        ComponentScan cs = cls.getAnnotation(ComponentScan.class);
        String[] pkgs = cs.basePackages();
        assertNotNull(pkgs);
        assertTrue(Arrays.asList(pkgs).contains("com.medgo.facescan"));
        assertTrue(Arrays.asList(pkgs).contains("com.medgo.crypto"));
        // negative check
        assertFalse(Arrays.asList(pkgs).contains("com.example"));
    }

    @Test
    void classHasEntityScan_withExpectedPackages() {
        Class<FaceScanServiceApplication> cls = FaceScanServiceApplication.class;
        assertTrue(cls.isAnnotationPresent(EntityScan.class), "@EntityScan should be present");

        EntityScan es = cls.getAnnotation(EntityScan.class);
        String[] pkgs = es.basePackages();
        assertNotNull(pkgs);
        assertTrue(Arrays.asList(pkgs).contains("com.medgo.facescan.domain.models.medgo"));
        assertTrue(Arrays.asList(pkgs).contains("com.medgo.facescan.domain.models.membership"));
    }

    @Test
    void mainMethod_exists_public_static_void() throws NoSuchMethodException {
        Method m = FaceScanServiceApplication.class.getMethod("main", String[].class);
        int mods = m.getModifiers();
        assertTrue(Modifier.isPublic(mods), "main should be public");
        assertTrue(Modifier.isStatic(mods), "main should be static");
        assertEquals(void.class, m.getReturnType(), "main should return void");
    }

    @Test
    void main_invokesSpringApplicationRun_withoutStartingContext() {
        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {
            mocked.when(() -> SpringApplication.run(FaceScanServiceApplication.class, new String[0])).thenReturn(null);

            FaceScanServiceApplication.main(new String[0]);

            mocked.verify(() -> SpringApplication.run(FaceScanServiceApplication.class, new String[0]));
        }
    }

    @Test
    void main_calls_spring_application_run() {
        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {
            String[] args = new String[0];
            FaceScanServiceApplication.main(args);
            mocked.verify(() -> SpringApplication.run(FaceScanServiceApplication.class, args));
        }
    }
}
