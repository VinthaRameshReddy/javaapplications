package com.medgo.member;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = MembershipServiceApplicationTests.class)
@ActiveProfiles("test")
@WithMockUser
class MembershipServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
