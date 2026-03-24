package com.medgo.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = AuthServiceApplicationTests.class)
@ActiveProfiles("test")
@WithMockUser
class AuthServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
