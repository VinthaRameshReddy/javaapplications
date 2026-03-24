package com.medgo.member;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest(classes = NotificationServiceApplicationTest.class)
@ActiveProfiles("test")
@WithMockUser
public class NotificationServiceApplicationTest {


    @Test
    void contextLoads() {
    }
}
