package com.starfleet.gamifier.repository;

import com.starfleet.gamifier.domain.Organization;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Organization aggregate root.
 * Single repository handling all configuration operations within the organization.
 */
@Repository
public interface OrganizationRepository extends MongoRepository<Organization, String> {

    Optional<Organization> findByName(String name);

    Optional<Organization> findByFederationId(String federationId);

    List<Organization> findByActiveTrue();

    boolean existsByName(String name);

    boolean existsByFederationId(String federationId);

    /**
     * Override the inherited findById method to add request-scoped caching.
     * Within a single HTTP request, repeated calls with the same organizationId
     * will return the cached result instead of querying MongoDB again.
     *
     * @param organizationId the organization ID to find
     * @return Optional containing the organization if found
     */
    @Override
    @Cacheable(value = "organizationById", cacheManager = "requestScopedCacheManager")
    Optional<Organization> findById(String organizationId);
}