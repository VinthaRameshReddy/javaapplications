package com.medgo.member;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = MemberServiceApplicationTests.class)
@ActiveProfiles("test")
@WithMockUser

class MemberServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
