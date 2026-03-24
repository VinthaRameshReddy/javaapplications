package com.medgo.member.config;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.*;

class FeignConfigTest {



    @Test
    void testAnnotationsPresentAndValues() {
        // Class loads -> line coverage
        FeignConfig config = new FeignConfig();
        assertNotNull(config);

        Configuration configuration = FeignConfig.class.getAnnotation(Configuration.class);
        assertNotNull(configuration, "@Configuration annotation missing");

        EnableFeignClients feign = FeignConfig.class.getAnnotation(EnableFeignClients.class);
        assertNotNull(feign, "@EnableFeignClients annotation missing");
        assertArrayEquals(new String[]{"com.medgo.member.feign"}, feign.basePackages(), "Feign basePackages mismatch");
    }
}

