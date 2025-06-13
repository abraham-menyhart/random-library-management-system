package com.library;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class LibraryManagementApplicationTest {

    @Test
    void contextLoads() {
        // given: Spring Boot application configuration
        // when: Spring context loads
        // then: no exceptions should be thrown
    }

}