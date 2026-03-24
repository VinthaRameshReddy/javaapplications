package com.medgo.claims;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest(classes = ClaimsServiceApplicationTests.class)
@ActiveProfiles("test")
@WithMockUser
class ClaimsServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
