package com.starfleet.gamifier.config;

import com.starfleet.gamifier.repository.OrganizationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.Cacheable;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple test to verify that the OrganizationRepository.findById method
 * is properly annotated with @Cacheable for request-scoped caching.
 */
class CachingAnnotationTest {

    @Test
    void testOrganizationRepositoryFindByIdHasCacheableAnnotation() throws Exception {
        // Get the findById method from OrganizationRepository
        Method findByIdMethod = OrganizationRepository.class.getMethod("findById", String.class);

        // Verify the method exists
        assertNotNull(findByIdMethod, "findById method should exist");

        // Verify the method has @Cacheable annotation
        Cacheable cacheableAnnotation = findByIdMethod.getAnnotation(Cacheable.class);
        assertNotNull(cacheableAnnotation, "findById method should have @Cacheable annotation");

        // Verify the cache configuration
        String[] cacheNames = cacheableAnnotation.value();
        assertEquals(1, cacheNames.length, "Should have exactly one cache name");
        assertEquals("organizationById", cacheNames[0], "Cache name should be 'organizationById'");

        // Verify the cache manager
        String cacheManager = cacheableAnnotation.cacheManager();
        assertEquals("requestScopedCacheManager", cacheManager, "Should use requestScopedCacheManager");

        // Verify return type
        assertEquals(Optional.class, findByIdMethod.getReturnType(), "Should return Optional<Organization>");
    }

    @Test
    void testCacheConfigClassExists() {
        // Verify that CacheConfig class exists and can be instantiated
        assertDoesNotThrow(() -> {
            CacheConfig config = new CacheConfig();
            assertNotNull(config, "CacheConfig should be instantiable");
        });
    }
}