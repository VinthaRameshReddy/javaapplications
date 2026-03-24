package com.medgo.virtualId;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest(classes = VirtualIdServiceApplicationTests.class)
@ActiveProfiles("test")
@WithMockUser
class VirtualIdServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
