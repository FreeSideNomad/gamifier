# Request-Scoped Caching Implementation

## Overview

This implementation provides request-scoped caching for the `OrganizationRepository.findById()` method to avoid repeated
MongoDB queries within a single HTTP request.

## Implementation Details

### 1. Cache Configuration (CacheConfig.java)

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @RequestScope
    public CacheManager requestScopedCacheManager() {
        return new ConcurrentMapCacheManager("organizationById");
    }
}
```

- **@EnableCaching**: Activates Spring's caching abstraction
- **@RequestScope**: Creates a new cache manager for each HTTP request
- **ConcurrentMapCacheManager**: In-memory cache implementation
- **organizationById**: Cache name used by the repository method

### 2. Repository Method Annotation (OrganizationRepository.java)

```java
@Override
@Cacheable(value = "organizationById", cacheManager = "requestScopedCacheManager")
Optional<Organization> findById(String organizationId);
```

- **@Cacheable**: Enables caching for this method
- **value**: Specifies the cache name ("organizationById")
- **cacheManager**: References our request-scoped cache manager

## Behavior

### Within a Single HTTP Request:

1. **First call** to `organizationRepository.findById("org-1")` → Hits MongoDB
2. **Second call** to `organizationRepository.findById("org-1")` → Served from cache
3. **Third call** to `organizationRepository.findById("org-1")` → Served from cache

### Across Different HTTP Requests:

1. **Request 1**: First call hits MongoDB, subsequent calls use cache
2. **Request 2**: Cache is cleared, first call hits MongoDB again
3. Each request gets its own fresh cache instance

## Benefits

- **Performance**: Eliminates duplicate database queries within request
- **Consistency**: Same organization data throughout request lifecycle
- **Memory**: No memory leaks (cache cleared after request)
- **Transparency**: No changes needed in service layer code

## Usage Example

```java
@Service
public class SomeService {

    @Autowired
    private OrganizationRepository organizationRepository;

    public void processRequest(String orgId) {
        // These calls within the same request will be cached:
        Organization org1 = organizationRepository.findById(orgId).orElse(null);
        Organization org2 = organizationRepository.findById(orgId).orElse(null); // From cache
        Organization org3 = organizationRepository.findById(orgId).orElse(null); // From cache

        // Only the first call hits MongoDB
    }
}
```

## Verification

To verify the caching is working:

1. **Add logging** to your MongoDB configuration to see actual queries
2. **Use application metrics** to monitor database connection usage
3. **Add debug logging** in services that call `findById()` multiple times
4. **Monitor response times** for requests that lookup the same organization multiple times

The implementation is transparent and requires no changes to existing service code while providing automatic performance
benefits for organization lookups within HTTP request boundaries.