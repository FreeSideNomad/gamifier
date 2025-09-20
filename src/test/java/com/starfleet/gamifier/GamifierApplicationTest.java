package com.starfleet.gamifier;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration test for the main application startup.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class GamifierApplicationTest {

    @Container
    static MongoDBContainer mongodb = new MongoDBContainer("mongo:7.0")
            .withExposedPorts(27017);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongodb::getReplicaSetUrl);
    }

    @Test
    void contextLoads() {
        // This test verifies that the Spring application context loads successfully
        // with all beans properly configured
    }

    @Test
    void mainMethodRuns() {
        // Test that the main method exists and can be referenced
        // We don't actually call main() here as it would start another application context
        // The contextLoads() test above verifies the application starts correctly
        assertDoesNotThrow(() -> {
            // Simply verify the main method exists by getting its reference
            GamifierApplication.class.getDeclaredMethod("main", String[].class);
        });
    }
}