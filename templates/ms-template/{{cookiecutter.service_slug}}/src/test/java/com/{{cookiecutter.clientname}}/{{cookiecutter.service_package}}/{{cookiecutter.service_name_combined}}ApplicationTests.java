package com.

{{cookiecutter.clientname}}.{{cookiecutter.service_package}};

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@ActiveProfiles("test")
@WithMockUser class {{cookiecutter.service_name_combined }}

ApplicationTests {

    @Test
    void contextLoads () {
    }

}
