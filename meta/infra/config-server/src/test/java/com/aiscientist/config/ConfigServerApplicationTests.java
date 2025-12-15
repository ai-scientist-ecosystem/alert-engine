package com.aiscientist.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.security.user.name=test",
    "spring.security.user.password=test",
    "spring.cloud.config.server.git.uri=https://github.com/spring-cloud-samples/config-repo",
    "eureka.client.enabled=false"
})
class ConfigServerApplicationTests {

    @Test
    void contextLoads() {
        // Verify that the Spring context loads successfully
    }
}
