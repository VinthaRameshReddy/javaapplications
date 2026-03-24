package com.medgo.member;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = UtilizationServiceApplicationTests.class)
@ActiveProfiles("test")
@WithMockUser
class UtilizationServiceApplicationTests {





    @Test
    void contextLoads() {
    }

}
