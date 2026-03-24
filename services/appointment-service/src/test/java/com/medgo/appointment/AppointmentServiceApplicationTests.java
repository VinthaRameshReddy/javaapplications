package com.medgo.appointment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest(classes = AppointmentServiceApplication.class)
@ActiveProfiles("test")
@WithMockUser
class AppointmentServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
